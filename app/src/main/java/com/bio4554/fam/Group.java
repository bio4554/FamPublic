package com.bio4554.fam;

import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by bio4554 on 6/3/2016.
 */
@IgnoreExtraProperties
public class Group {
    private String name;
    private String creatorid;
    private int members;

    public Group() {

    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCreatorid(String creatorid) {
        this.creatorid = creatorid;
    }

    public void setMembers(int members) {
        this.members = members;
    }

    public Group(String n, String c, int m) {
        name = n;
        creatorid = c;
        members = m;

    }

    public String getName() {
        return this.name;
    }

    public String getCreatorid() {
        return this.creatorid;
    }

    public int getMembers() {
        return this.members;
    }

    public void addMember() {
        members++;
    }
}
