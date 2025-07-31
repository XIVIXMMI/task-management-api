package com.omori.taskmanagement.springboot.model.audit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ActionType {
    CREATE,
    UPDATE,
    DELETE,
    VIEW,
    COMMENT,
    ASSIGN,
    COMPLETE,
    REOPEN,
    INVITE,
    ACCEPT,
    REJECT,
    UPLOAD,
    DOWNLOAD,
    SHARE,
    UNSHARE,
    LOGIN,
    REGISTER,
    LOGOUT,
    FAILED_LOGIN;

    public static ActionType fromMethod(String method) {
        switch (method.toLowerCase()) {
            case "create":
                return CREATE;
            case "update":
                return UPDATE;
            case "delete":
                return DELETE;
            case "view":
                return VIEW;
            case "comment":
                return COMMENT;
            case "assign":
                return ASSIGN;
            case "complete":
                return COMPLETE;
            case "reopen":
                return REOPEN;
            case "invite":
                return INVITE;
            case "accept":
                return ACCEPT;
            case "reject":
                return REJECT;
            case "upload":
                return UPLOAD;
            case "download":
                return DOWNLOAD;
            case "share":
                return SHARE;
            case "unshare":
                return UNSHARE;
            case "login":
                return LOGIN;
            case "register":
                return REGISTER;
            case "logout":
                return LOGOUT;
            case "failedlogin":
                return FAILED_LOGIN;
            default:
                log.warn("Unknown action type: " + method);
                return null;
        }
    }
}
