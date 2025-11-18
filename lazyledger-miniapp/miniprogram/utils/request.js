const app = getApp();

function request(options) {
  return new Promise((resolve, reject) => {
    const { apiBase, userId } = app.globalData;
    wx.request({
      url: apiBase + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: Object.assign({
        'Content-Type': 'application/json',
        'X-User-Id': userId || ''
      }, options.header || {}),
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data);
        } else {
          wx.showToast({ title: res.data?.message || '请求失败', icon: 'none' });
          reject(res.data);
        }
      },
      fail: reject
    });
  });
}

module.exports = {
  request
};
