package com.xcz.controller;

import com.xcz.enums.VideoStatusEnum;
import com.xcz.pojo.Bgm;
import com.xcz.pojo.Comments;
import com.xcz.pojo.Videos;
import com.xcz.service.BgmService;
import com.xcz.service.VideoService;
import com.xcz.utils.FetchVideoCover;
import com.xcz.utils.IMoocJSONResult;
import com.xcz.utils.MergeVideoMp3;
import com.xcz.utils.PagedResult;
import io.swagger.annotations.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;


@RestController
@Api(value="视频相关业务的接口", tags= {"视频相关业务的controller"})
@RequestMapping("/video")
public class VideoController extends BasicController {
	
	@Autowired
	private BgmService bgmService;
	
	@Autowired
	private VideoService videoService;
	
	@ApiOperation(value="上传视频", notes="上传视频的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="bgmId", value="背景音乐id", required=false, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoSeconds", value="背景音乐播放长度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoWidth", value="视频宽度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoHeight", value="视频高度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="desc", value="视频描述", required=false, 
				dataType="String", paramType="form")
	})
	@PostMapping(value="/upload", headers="content-type=multipart/form-data")
	public IMoocJSONResult upload(String userId,
								  String bgmId, double videoSeconds,
								  int videoWidth, int videoHeight,
								  String desc,
								  @ApiParam(value="短视频", required=true)
										  MultipartFile file) throws Exception {

		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.errorMsg("用户id不能为空...");
		}

		// 文件保存的命名空间
//		String fileSpace = "C:/imooc_videos_dev";
		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		String coverPathDB = "/" + userId + "/video";

		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		// 文件上传的最终保存路径
		String finalVideoPath = "";
		try {
			if (file != null) {

				String fileName = file.getOriginalFilename();
				// abc.mp4
				String arrayFilenameItem[] =  fileName.split("\\.");
				String fileNamePrefix = "";
				for (int i = 0 ; i < arrayFilenameItem.length-1 ; i ++) {
					fileNamePrefix += arrayFilenameItem[i];
				}
				// fix bug: 解决小程序端OK，PC端不OK的bug，原因：PC端和小程序端对临时视频的命名不同
//				String fileNamePrefix = fileName.split("\\.")[0];

				if (StringUtils.isNotBlank(fileName)) {

					finalVideoPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					// 设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";

					File outFile = new File(finalVideoPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						// 创建父文件夹
						outFile.getParentFile().mkdirs();
					}

					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}

			} else {
				return IMoocJSONResult.errorMsg("上传出错...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return IMoocJSONResult.errorMsg("上传出错...");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}

		// 判断bgmId是否为空，如果不为空，
		// 那就查询bgm的信息，并且合并视频，生产新的视频
		if (StringUtils.isNotBlank(bgmId)) {
			Bgm bgm = bgmService.queryBgmById(bgmId);
			String mp3InputPath = FILE_SPACE + bgm.getPath();

			MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
			String videoInputPath = finalVideoPath;

			String videoOutputName = UUID.randomUUID().toString() + ".mp4";
			uploadPathDB = "/" + userId + "/video" + "/" + videoOutputName;
			finalVideoPath = FILE_SPACE + uploadPathDB;
			tool.convertor(videoInputPath, mp3InputPath, videoSeconds, finalVideoPath);
		}
		System.out.println("uploadPathDB=" + uploadPathDB);
		System.out.println("finalVideoPath=" + finalVideoPath);

		// 对视频进行截图
		FetchVideoCover videoInfo = new FetchVideoCover(FFMPEG_EXE);
		videoInfo.getCover(finalVideoPath, FILE_SPACE + coverPathDB);

		// 保存视频信息到数据库
		Videos video = new Videos();
		video.setAudioId(bgmId);
		video.setUserId(userId);
		video.setVideoSeconds((float)videoSeconds);
		video.setVideoHeight(videoHeight);
		video.setVideoWidth(videoWidth);
		video.setVideoDesc(desc);
		video.setVideoPath(uploadPathDB);
		video.setCoverPath(coverPathDB);
		video.setStatus(VideoStatusEnum.SUCCESS.value);
		video.setCreateTime(new Date());

		String videoId = videoService.saveVideo(video);

		return IMoocJSONResult.ok(videoId);
	}

	/**
	 *
	 * @Description: 分页和搜索查询视频列表
	 * isSaveRecord：1 - 需要保存
	 * 				 0 - 不需要保存 ，或者为空的时候
	 */
	@PostMapping(value="/showAll")
	public IMoocJSONResult showAll(@RequestBody Videos video, Integer isSaveRecord,
								   Integer page, Integer pageSize) throws Exception {

		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = PAGE_SIZE;
		}

		PagedResult result = videoService.getAllVideos(video, isSaveRecord, page, pageSize);
		return IMoocJSONResult.ok(result);
	}

	/**
	 * 热词
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/hot")
	public IMoocJSONResult hot() throws Exception {
		return IMoocJSONResult.ok(videoService.getHotwords());
	}

	/**
	 * 点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/userLike")
	public IMoocJSONResult userLike(String userId, String videoId, String videoCreaterId)
			throws Exception {
		videoService.userLikeVideo(userId, videoId, videoCreaterId);
		return IMoocJSONResult.ok();
	}

	/**
	 * 取消点赞视频
	 * @param userId
	 * @param videoId
	 * @param videoCreaterId
	 * @return
	 * @throws Exception
	 */
	@PostMapping(value="/userUnLike")
	public IMoocJSONResult userUnLike(String userId, String videoId, String videoCreaterId) throws Exception {
		videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
		return IMoocJSONResult.ok();
	}

	/**
	 * @Description: 我关注的人发的视频
	 */
	@PostMapping("/showMyFollow")
	public IMoocJSONResult showMyFollow(String userId, Integer page) throws Exception {

		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.ok();
		}

		if (page == null) {
			page = 1;
		}

		int pageSize = 6;

		PagedResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);

		return IMoocJSONResult.ok(videosList);
	}

	/**
	 * @Description: 我收藏(点赞)过的视频列表
	 */
	@PostMapping("/showMyLike")
	public IMoocJSONResult showMyLike(String userId, Integer page, Integer pageSize) throws Exception {

		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.ok();
		}

		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 6;
		}

		PagedResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);

		return IMoocJSONResult.ok(videosList);
	}


	@PostMapping("/saveComment")
	public IMoocJSONResult saveComment(@RequestBody Comments comment,
									   String fatherCommentId, String toUserId) throws Exception {

		comment.setFatherCommentId(fatherCommentId);
		comment.setToUserId(toUserId);

		videoService.saveComment(comment);
		return IMoocJSONResult.ok();
	}

	@PostMapping("/getVideoComments")
	public IMoocJSONResult getVideoComments(String videoId, Integer page, Integer pageSize) throws Exception {

		if (StringUtils.isBlank(videoId)) {
			return IMoocJSONResult.ok();
		}

		// 分页查询视频列表，时间顺序倒序排序
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 10;
		}

		PagedResult list = videoService.getAllComments(videoId, page, pageSize);

		return IMoocJSONResult.ok(list);
	}
}
