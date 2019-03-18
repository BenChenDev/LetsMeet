package com.example.benjamin.letsmeet;

public class Group {
    private String GroupID;
    private String Topic;
    private String Member;

    public Group(){}

    public Group(String groupID, String topic, String member) {
        GroupID = groupID;
        Topic = topic;
        Member = member;
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

    public void setGroupID(String groupID) {
        GroupID = groupID;
    }

    public void setTopic(String topic) {
        Topic = topic;
    }

    public void setMember(String member) {
        Member = member;
    }
}
