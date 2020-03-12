package com.xcz.mapper;

import com.xcz.pojo.Comments;
import com.xcz.pojo.vo.CommentsVO;
import com.xcz.utils.MyMapper;

import java.util.List;

public interface CommentsMapperCustom extends MyMapper<Comments> {
	
	public List<CommentsVO> queryComments(String videoId);
}