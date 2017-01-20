package com.bio4554.fam;

/**
 * Created by bio4554 on 6/4/2016.
 */

public class Post {
    String uid;
    String message;
    String name;

    public Post() {

    }

    public Post(String uid, String message, String name) {
        this.message = message;
        this.name = name;
        this.uid = uid;
    }

    public String getUid() {
        return this.uid;
    }

    public String getName() {
        return this.name;
    }

    public String getMessage() {
        return this.message;
    }
}
