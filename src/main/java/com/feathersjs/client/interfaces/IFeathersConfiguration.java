package com.feathersjs.client.interfaces;

import com.feathersjs.client.Feathers;

public class IFeathersConfiguration extends IFeathersConfigurable {
    private Feathers mApp;

    public void setApp(Feathers feathers) {
        mApp = feathers;
    }
}
