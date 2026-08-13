package com.github.jothammicheni.daraja.config;

public class MpesaConfig {
    private final String consumerKey;
    private final String consumerSecret;
    private final String apiSecret;
    private final MpesaEnvironment environment;
    private final String baseUrl;
    private final int connectTimeout;
    private final int readTimeout;
    private final String appUrl;
    private final String callbackUrl;
    private final String cacheType;
    private final String confirmationUrlPath;
    private final String validationUrlPath;
    private final boolean enableIpValidation;
    private final boolean isBehindProxy;

    private MpesaConfig(Builder builder) {
        this.consumerKey = builder.consumerKey;
        this.consumerSecret = builder.consumerSecret;
        this.apiSecret = builder.apiSecret;
        this.environment = builder.environment != null ? builder.environment : MpesaEnvironment.SANDBOX;

        if (builder.baseUrl == null || builder.baseUrl.isBlank()) {
            this.baseUrl = (this.environment == MpesaEnvironment.PRODUCTION)
                    ? "https://safaricom.co.ke"
                    : "https://sandbox.safaricom.co.ke";
        } else {
            this.baseUrl = builder.baseUrl;
        }

        this.connectTimeout = builder.connectTimeout > 0 ? builder.connectTimeout : 10;
        this.readTimeout = builder.readTimeout > 0 ? builder.readTimeout : 30;
        this.appUrl = builder.appUrl != null ? builder.appUrl : "http://localhost:8080";
        this.cacheType = builder.cacheType != null ? builder.cacheType : "local";
        this.confirmationUrlPath = builder.confirmationUrlPath != null ? builder.confirmationUrlPath : "/cb/confirmation";
        this.validationUrlPath = builder.validationUrlPath != null ? builder.validationUrlPath : "/cb/validation";
        this.enableIpValidation = builder.enableIpValidation;
        this.isBehindProxy = builder.isBehindProxy;

        // Build callback URL
        if (builder.callbackUrl == null || builder.callbackUrl.isBlank()) {
            String cleanAppUrl = this.appUrl.endsWith("/") ?
                    this.appUrl.substring(0, this.appUrl.length() - 1) : this.appUrl;
            String cleanPath = this.confirmationUrlPath.startsWith("/") ?
                    this.confirmationUrlPath : "/" + this.confirmationUrlPath;
            this.callbackUrl = cleanAppUrl + cleanPath;
        } else {
            this.callbackUrl = builder.callbackUrl;
        }
    }

    // Getters
    public String getConsumerKey() { return consumerKey; }
    public String getConsumerSecret() { return consumerSecret; }
    public String getApiSecret() { return apiSecret; }
    public MpesaEnvironment getEnvironment() { return environment; }
    public String getBaseUrl() { return baseUrl; }
    public int getConnectTimeout() { return connectTimeout; }
    public int getReadTimeout() { return readTimeout; }
    public String getAppUrl() { return appUrl; }
    public String getCallbackUrl() { return callbackUrl; }
    public String getCacheType() { return cacheType; }
    public String getConfirmationUrlPath() { return confirmationUrlPath; }
    public String getValidationUrlPath() { return validationUrlPath; }
    public boolean isEnableIpValidation() { return enableIpValidation; }
    public boolean isBehindProxy() { return isBehindProxy; }

    public static class Builder {
        private final String consumerKey;
        private final String consumerSecret;
        private final String apiSecret;
        private MpesaEnvironment environment;
        private String baseUrl;
        private int connectTimeout;
        private int readTimeout;
        private String appUrl;
        private String callbackUrl;
        private String cacheType;
        private String confirmationUrlPath;
        private String validationUrlPath;
        private boolean enableIpValidation;
        private boolean isBehindProxy;

        public Builder(String consumerKey, String consumerSecret, String apiSecret) {
            this.consumerKey = consumerKey;
            this.consumerSecret = consumerSecret;
            this.apiSecret = apiSecret;
        }

        public Builder environment(MpesaEnvironment environment) { this.environment = environment; return this; }
        public Builder baseUrl(String baseUrl) { this.baseUrl = baseUrl; return this; }
        public Builder connectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; return this; }
        public Builder readTimeout(int readTimeout) { this.readTimeout = readTimeout; return this; }
        public Builder appUrl(String appUrl) { this.appUrl = appUrl; return this; }
        public Builder callbackUrl(String callbackUrl) { this.callbackUrl = callbackUrl; return this; }
        public Builder cacheType(String cacheType) { this.cacheType = cacheType; return this; }
        public Builder confirmationUrlPath(String confirmationUrlPath) { this.confirmationUrlPath = confirmationUrlPath; return this; }
        public Builder validationUrlPath(String validationUrlPath) { this.validationUrlPath = validationUrlPath; return this; }
        public Builder enableIpValidation(boolean enableIpValidation) { this.enableIpValidation = enableIpValidation; return this; }
        public Builder isBehindProxy(boolean isBehindProxy) { this.isBehindProxy = isBehindProxy; return this; }

        public MpesaConfig build() {
            return new MpesaConfig(this);
        }
    }
}