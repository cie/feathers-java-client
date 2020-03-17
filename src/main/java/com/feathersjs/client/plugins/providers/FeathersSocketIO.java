package com.feathersjs.client.plugins.providers;

import io.socket.client.IO;

public class FeathersSocketIO extends IProviderConfiguration {

    public static class Options extends IO.Options {
//        public String baseUrl;
    }

    private final Options mOptions;

    public FeathersSocketIO(Options options) {
       mOptions = options;
    }

    public FeathersSocketIO() {
        mOptions = new Options();
    }
}
