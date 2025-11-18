package com.lazyledger.importer.remote.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "lazyledger.remote")
public class RemoteStatementProperties {

    private Provider wechat = new Provider();
    private Provider alipay = new Provider();

    public Provider getWechat() {
        return wechat;
    }

    public void setWechat(Provider wechat) {
        this.wechat = wechat;
    }

    public Provider getAlipay() {
        return alipay;
    }

    public void setAlipay(Provider alipay) {
        this.alipay = alipay;
    }

    public static class Provider {
        private String billUrl;

        public String getBillUrl() {
            return billUrl;
        }

        public void setBillUrl(String billUrl) {
            this.billUrl = billUrl;
        }
    }
}
