const { request } = require('../../utils/request');

Page({
  data: {
    phone: '',
    nickname: ''
  },
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value });
  },
  onNicknameInput(e) {
    this.setData({ nickname: e.detail.value });
  },
  async login() {
    if (!this.data.phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    try {
      const res = await request({
        url: '/api/auth/wx-login',
        method: 'POST',
        data: {
          code: 'demo-code',
          phone: this.data.phone,
          nickname: this.data.nickname
        }
      });
      const { data } = res;
      const app = getApp();
      app.globalData.userId = data.userId;
      app.globalData.token = data.token;
      wx.setStorageSync('user', data);
      wx.showToast({ title: '登录成功' });
      wx.switchTab({ url: '/pages/index/index' });
    } catch (err) {
      console.error(err);
    }
  }
});
