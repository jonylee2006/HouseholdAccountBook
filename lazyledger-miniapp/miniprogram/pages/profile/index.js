const { request } = require('../../utils/request');

Page({
  data: {
    userId: null,
    ledgerId: '',
    ledgerName: '',
    currency: '',
    monthlyBudget: '',
    level: '',
    expiresAt: ''
  },
  onShow() {
    const app = getApp();
    this.setData({
      userId: app.globalData.userId,
      ledgerId: app.globalData.currentLedgerId || ''
    });
  },
  goLogin() {
    wx.navigateTo({ url: '/pages/login/index' });
  },
  onLedgerInput(e) { this.setData({ ledgerId: e.detail.value }); },
  onLedgerNameInput(e) { this.setData({ ledgerName: e.detail.value }); },
  onCurrencyInput(e) { this.setData({ currency: e.detail.value }); },
  onBudgetInput(e) { this.setData({ monthlyBudget: e.detail.value }); },
  onLevelInput(e) { this.setData({ level: e.detail.value }); },
  onExpireInput(e) { this.setData({ expiresAt: e.detail.value }); },
  setLedger() {
    const ledgerId = this.data.ledgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请输入账本ID', icon: 'none' });
      return;
    }
    const app = getApp();
    app.globalData.currentLedgerId = Number(ledgerId);
    wx.setStorageSync('ledgerId', Number(ledgerId));
    wx.showToast({ title: '已切换账本' });
  },
  async createLedger() {
    if (!this.data.ledgerName) {
      wx.showToast({ title: '请输入名称', icon: 'none' });
      return;
    }
    try {
      const res = await request({
        url: '/api/ledger',
        method: 'POST',
        data: {
          name: this.data.ledgerName,
          currency: this.data.currency || 'CNY',
          monthlyBudget: this.data.monthlyBudget ? Number(this.data.monthlyBudget) : null
        }
      });
      const ledger = res.data;
      const app = getApp();
      app.globalData.currentLedgerId = ledger.id;
      wx.setStorageSync('ledgerId', ledger.id);
      wx.showToast({ title: '创建成功' });
      this.setData({ ledgerId: ledger.id });
    } catch (err) {
      console.error(err);
    }
  },
  async updateSubscription() {
    const app = getApp();
    const ledgerId = app.globalData.currentLedgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请先设置账本', icon: 'none' });
      return;
    }
    try {
      await request({
        url: '/v1/subscription',
        method: 'POST',
        data: {
          ledgerId,
          level: this.data.level || 'free',
          expiresAt: this.data.expiresAt || null
        }
      });
      wx.showToast({ title: '已更新订阅' });
    } catch (err) {
      console.error(err);
    }
  }
});
