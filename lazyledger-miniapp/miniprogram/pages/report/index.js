const { request } = require('../../utils/request');

function formatPeriod(dateStr) {
  const [year, month] = dateStr.split('-');
  return `${year}-${month}`;
}

Page({
  data: {
    period: formatPeriod(new Date().toISOString().slice(0, 7)),
    report: null
  },
  onPeriodChange(e) {
    this.setData({ period: formatPeriod(e.detail.value) });
  },
  async loadReport() {
    const app = getApp();
    const ledgerId = app.globalData.currentLedgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请先选择账本', icon: 'none' });
      return;
    }
    const [year, month] = this.data.period.split('-');
    try {
      const res = await request({
        url: '/v1/report/monthly',
        data: { ledgerId, year: Number(year), month: Number(month) }
      });
      this.setData({ report: res.data });
    } catch (err) {
      console.error(err);
    }
  }
});
