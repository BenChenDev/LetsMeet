package com.example.benjamin.letsmeet;

public class Group {
    private String GroupID;
    private String Topic;
    private String Member;
    private String Group_Owner_id;
    private String Group_Owner_name;
    private String Member_name;
    private String Location;

    public Group(){}

    public Group(String groupID, String topic, String member, String group_Owner_id, String group_Owner_name, String member_name, String location) {
        GroupID = groupID;
        Topic = topic;
        Member = member;
        Group_Owner_id = group_Owner_id;
        Group_Owner_name = group_Owner_name;
        Member_name = member_name;
        Location = location;
    }

    public String getGroupID() {
        return GroupID;
    }

    public String getTopic() {
        return Topic;
    }

    public String getMember() {
        return Member;
    }

    public String getLocation() {
        return Location;
    }

    public void setGroupID(String groupID) {
        GroupID = groupID;
    }

    public void setTopic(String topic) {
        Topic = topic;
    }

    public void setMember(String member) {
        Member = member;
    }

    public String getGroup_Owner_id() {
        return Group_Owner_id;
    }

    public String getGroup_Owner_name() {
        return Group_Owner_name;
    }

    public String getMember_name() {
        return Member_name;
    }

    public void setGroup_Owner_id(String group_Owner_id) {
        Group_Owner_id = group_Owner_id;
    }

    public void setGroup_Owner_name(String group_Owner_name) {
        Group_Owner_name = group_Owner_name;
    }

    public void setMember_name(String member_name) {
        Member_name = member_name;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
