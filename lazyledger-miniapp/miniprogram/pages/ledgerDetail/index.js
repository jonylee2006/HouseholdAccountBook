const { request } = require('../../utils/request');

Page({
  data: {
    jobId: '',
    transactions: []
  },
  onJobInput(e) {
    this.setData({ jobId: e.detail.value });
  },
  async loadTransactions() {
    if (!this.data.jobId) {
      wx.showToast({ title: '请输入任务ID', icon: 'none' });
      return;
    }
    try {
      const res = await request({
        url: `/v1/import/jobs/${this.data.jobId}/transactions`
      });
      this.setData({ transactions: res.data || [] });
    } catch (err) {
      console.error(err);
    }
  }
});
