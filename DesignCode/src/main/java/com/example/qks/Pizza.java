package com.example.qks;

public abstract class Pizza {

    protected String name;

    public abstract void prepare();

    public void bake() {}
    public void cut() {}
    public void box() {}
    public void setName(String name) {
        this.name = name;
    }
}
