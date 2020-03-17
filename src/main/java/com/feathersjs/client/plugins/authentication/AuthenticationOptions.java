package com.feathersjs.client.plugins.authentication;

import com.feathersjs.client.plugins.storage.IStorageProvider;

public class AuthenticationOptions {
    public String cookie;
    public String tokenKey;
    public String localEndpoint;
    public String tokenEndpoint;
    public String usernameField;

    public IStorageProvider storage;
}
