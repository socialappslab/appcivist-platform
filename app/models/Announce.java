package models;


import enums.MessageType;

import java.util.Date;

public class Announce extends Message{

    public Announce(User creator, Date creation, Date removal, String lang, Long messageId, String title, String text, MessageType type, User targetUser, WorkingGroup targeWorkingGroup, Assembly assembly, Organization organization) {
        super(creator, creation, removal, lang, messageId, title, text, type, targetUser, targeWorkingGroup, assembly, organization);
    }
}
