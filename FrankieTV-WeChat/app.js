//app.js
App({
  serverUrl: "http://192.168.25.129:8080",

  // serverUrl: "http://127.0.0.1:8081", 本地部署
  // serverUrl:"http://xcz.free.idcfengye.com",ngrok部署
  //serverUrl: "http://121.36.15.92:8080", //云部署
  userInfo: null,

  setGlobalUserInfo: function(user) {
    wx.setStorageSync("userInfo", user);
  },

  getGlobalUserInfo: function() {
    return wx.getStorageSync("userInfo");
  },

  reportReasonArray: [
    "色情低俗",
    "政治敏感",
    "涉嫌诈骗",
    "辱骂谩骂",
    "广告垃圾",
    "诱导分享",
    "引人不适",
    "过于暴力",
    "违法违纪",
    "其它原因"
  ]
})