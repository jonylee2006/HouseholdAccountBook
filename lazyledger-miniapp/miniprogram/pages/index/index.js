const { request } = require('../../utils/request');

Page({
  data: {
    todaySpent: '0.00',
    monthBudget: '0.00',
    monthRemaining: '0.00',
    topCategories: []
  },
  onShow() {
    this.loadSummary();
  },
  async loadSummary() {
    const app = getApp();
    const ledgerId = app.globalData.currentLedgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请先选择账本', icon: 'none' });
      return;
    }
    try {
      const res = await request({
        url: '/api/dashboard/summary',
        data: { ledgerId }
      });
      const data = res.data;
      this.setData({
        todaySpent: Number(data.todaySpent || 0).toFixed(2),
        monthBudget: Number(data.monthBudget || 0).toFixed(2),
        monthRemaining: Number(data.monthRemaining || 0).toFixed(2),
        topCategories: data.topCategories || []
      });
    } catch (err) {
      console.error(err);
    }
  },
  goLedgerDetail() {
    wx.navigateTo({ url: '/pages/ledgerDetail/index' });
  }
});
