package com.github.polimi_mt_acg.back2school.api.v1;

public class Credentials {
    public String email;
    public String password;

    public Credentials() {
    }

    public Credentials(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
