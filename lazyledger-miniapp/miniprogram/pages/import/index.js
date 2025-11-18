const { request } = require('../../utils/request');

Page({
  data: {
    sources: ['WECHAT', 'ALIPAY'],
    sourceIndex: 0,
    statementDate: new Date().toISOString().slice(0, 10),
    authorizationId: '',
    authDate: new Date().toISOString().slice(0, 10),
    uploadStatus: ''
  },
  onSourceChange(e) {
    this.setData({ sourceIndex: Number(e.detail.value) });
  },
  onDateChange(e) {
    this.setData({ statementDate: e.detail.value });
  },
  onAuthInput(e) {
    this.setData({ authorizationId: e.detail.value });
  },
  onAuthDateChange(e) {
    this.setData({ authDate: e.detail.value });
  },
  async chooseFile() {
    try {
      const fileRes = await wx.chooseMessageFile({ count: 1, type: 'file', extension: ['csv'] });
      const filePath = fileRes.tempFiles[0].path;
      this.setData({ uploadStatus: '上传中...' });
      const uploadInfo = await request({
        url: '/api/import/upload-url',
        method: 'POST',
        data: { directory: 'statements', contentType: 'text/csv' }
      });
      const { url, objectKey, headers = {} } = uploadInfo.data;
      await wx.uploadFile({
        url,
        filePath,
        name: 'file',
        header: headers,
        formData: {},
        success: () => wx.showToast({ title: '上传成功' }),
        fail: (err) => { throw err; }
      });
      await this.submitImport(objectKey);
      this.setData({ uploadStatus: '导入任务已提交' });
    } catch (err) {
      console.error(err);
      wx.showToast({ title: '导入失败', icon: 'none' });
      this.setData({ uploadStatus: '' });
    }
  },
  async submitImport(objectKey) {
    const app = getApp();
    const ledgerId = app.globalData.currentLedgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请先在“我的”页选择账本', icon: 'none' });
      throw new Error('no ledger');
    }
    return request({
      url: '/api/import/parse-csv',
      method: 'POST',
      data: {
        ledgerId,
        sourceType: this.data.sources[this.data.sourceIndex],
        objectKey,
        statementDate: this.data.statementDate
      }
    });
  },
  async importByAuthorization() {
    if (!this.data.authorizationId) {
      wx.showToast({ title: '请输入授权ID', icon: 'none' });
      return;
    }
    const app = getApp();
    const ledgerId = app.globalData.currentLedgerId;
    if (!ledgerId) {
      wx.showToast({ title: '请先设置账本', icon: 'none' });
      return;
    }
    try {
      await request({
        url: '/api/import/parse-authorization',
        method: 'POST',
        data: {
          ledgerId,
          authorizationId: Number(this.data.authorizationId),
          statementDate: this.data.authDate
        }
      });
      wx.showToast({ title: '已提交授权导入' });
    } catch (err) {
      console.error(err);
    }
  }
});
