package com.bio4554.fam;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by bio4554 on 6/3/2016.
 */
@IgnoreExtraProperties
public class User {
    private String username;
    private String email;
    private String group;
    private Boolean ingroup;

    public User() {

    }

    public User(String u, String e, String g, Boolean ig) {
        this.email = e;
        this.username = u;
        this.group = g;
        this.ingroup = ig;
    }

    public String getUsername() {
        return this.username;
    }

    public String getEmail() {
        return this.email;
    }

    public String getGroup() {
        return this.group;
    }

    public Boolean getIngroup() {
        return this.ingroup;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setIngroup(Boolean ingroup) {
        this.ingroup = ingroup;
    }
}
