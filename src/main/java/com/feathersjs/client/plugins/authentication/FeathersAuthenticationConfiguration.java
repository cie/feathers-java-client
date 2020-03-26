package com.feathersjs.client.plugins.authentication;

@Deprecated
public class FeathersAuthenticationConfiguration {
    private final AuthenticationOptions mOptions;

    public FeathersAuthenticationConfiguration() {
        mOptions = new AuthenticationOptions();
    }

    public FeathersAuthenticationConfiguration(AuthenticationOptions options) {
        mOptions = options;
    }

    public AuthenticationOptions getOptions() {
        return mOptions;
    }

}
