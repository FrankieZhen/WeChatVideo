function uploadVideo() {
  var me = this;

  wx.chooseVideo({
    sourceType: ['album'],
    success: function(res) {
      console.log(res);

      var duration = res.duration;
      var height = res.height;
      var width = res.width;
      var tempFilePath = res.tempFilePath;
      var thumbTempFilePath = res.thumbTempFilePath;

      if (duration > 30) {
        wx.showToast({
          title: '视频长度不能超过30秒...',
          icon: "none",
          duration: 2500
        })
      } else if (duration < 1) {
        wx.showToast({
          title: '视频长度太短，请上传超过1秒的视频...',
          icon: "none",
          duration: 2500
        })
      } else {
        // 打开选择bgm的页面
        wx.navigateTo({
          url: '../chooseBgm/chooseBgm?duration=' + duration +
            "&height=" + height +
            "&width=" + width +
            "&tempFilePath=" + tempFilePath +
            "&thumbTempFilePath=" + thumbTempFilePath,
        })
      }

    }
  })

}

module.exports = {
  uploadVideo: uploadVideo
}