App({
  globalData: {
    apiBase: 'https://api.example.com',
    userId: null,
    token: null,
    currentLedgerId: null
  },
  onLaunch() {
    const storedUser = wx.getStorageSync('user');
    if (storedUser) {
      this.globalData.userId = storedUser.userId;
      this.globalData.token = storedUser.token;
    }
    const ledgerId = wx.getStorageSync('ledgerId');
    if (ledgerId) {
      this.globalData.currentLedgerId = ledgerId;
    }
  }
});
