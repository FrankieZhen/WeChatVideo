const app = getApp()

Page({
  data: {
    bgmList: [],
    serverUrl: "",
    videoParams: {}
  },

  onLoad: function(params) {

    var me = this;
    var user = app.getGlobalUserInfo();
    console.log(params);
    me.setData({
      videoParams: params
    })
    wx.showLoading({
      title: '请等待...',
    });
    var serverUrl = app.serverUrl;
    // 调用后端
    wx.request({
      url: serverUrl + '/bgm/list',
      method: "POST",
      header: {
        'content-type': 'application/json', // 默认值
        'headerUserId': user.id,
        'headerUserToken': user.userToken
      },
      success: function(res) {
        console.log(res.data);
        wx.hideLoading();
        if (res.data.status == 200) {
          var bgmList = res.data.data;
          me.setData({
            bgmList: bgmList,
            serverUrl: serverUrl
          });
        }
        // else if (res.data.status == 502) {
        //   wx.showToast({
        //     title: res.data.msg,
        //     duration: 2000,
        //     icon: "none",
        //     success: function () {
        //       wx.redirectTo({
        //         url: '../userLogin/login',
        //       })
        //     }
        //   });
        // }
      }
    })
  },

  upload: function(e) {
    var me = this;
    var bgmId = e.detail.value.bgmId;
    var desc = e.detail.value.desc;

    console.log("bgmId:" + bgmId);
    console.log("desc:" + desc);

    var duration = me.data.videoParams.duration;
    var height = me.data.videoParams.height;
    var width = me.data.videoParams.width;
    var tempFilePath = me.data.videoParams.tempFilePath;
    var thumbTempFilePath = me.data.videoParams.thumbTempFilePath;


    /**
     *上传视频
     */
    var serverUrl = app.serverUrl;
    var user = app.getGlobalUserInfo();
    //var userInfo = app.getGlobalUserInfo();
    wx.uploadFile({
      url: serverUrl + '/video/upload',
      formData: {
        userId: user.id, // fixme 原来的 app.userInfo.id
        bgmId: bgmId,
        desc: desc,
        videoSeconds: duration,
        videoHeight: height,
        videoWidth: width
      },
      filePath: tempFilePath,
      name: 'file',
      header: {
        'headerUserId': user.id,
        'headerUserToken': user.userToken
      },
      success: function(res) {
        var data = JSON.parse(res.data);
        console.log(data);
        wx.hideLoading();
        if (data.status == 200) {
          wx.showToast({
            title: '上传成功!~~',
            icon: "success",
            duration: 1000
          });
          // 页面跳转
          wx.redirectTo({
            url: '../mine/mine',
          })

        }

      }
    })
  }

})