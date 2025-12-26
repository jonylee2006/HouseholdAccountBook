const { request } = require('../../utils/request');

Page({
  data: {
    expireMinutes: 60,
    inviteToken: '',
    token: ''
  },
  onExpireInput(e) {
    this.setData({ expireMinutes: Number(e.detail.value) });
  },
  onTokenInput(e) {
    this.setData({ token: e.detail.value });
  },
  async createInvite() {
    const app = getApp();
    const ledgerId = app.globalData.currentLedgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请先选择账本', icon: 'none' });
      return;
    }
    try {
      const res = await request({
        url: `/api/ledger/${ledgerId}/invite`,
        method: 'POST',
        data: { expireMinutes: this.data.expireMinutes }
      });
      this.setData({ inviteToken: res.data.token });
    } catch (err) {
      console.error(err);
    }
  },
  async acceptInvite() {
    if (!this.data.token) {
      wx.showToast({ title: '请输入 token', icon: 'none' });
      return;
    }
    try {
      await request({
        url: `/api/ledger/invite/${this.data.token}/accept`,
        method: 'POST'
      });
      wx.showToast({ title: '加入成功' });
    } catch (err) {
      console.error(err);
    }
  }
});
