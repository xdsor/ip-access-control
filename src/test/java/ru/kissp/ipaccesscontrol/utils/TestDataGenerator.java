package ru.kissp.ipaccesscontrol.utils;

import ru.kissp.ipaccesscontrol.appuser.adapter.dto.CreateNewUserRequest;
import ru.kissp.ipaccesscontrol.appuser.domain.AppUser;
import ru.kissp.ipaccesscontrol.ipaccess.domain.IpAccess;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestDataGenerator {
    public static AppUser createAppUser() {
        return new AppUser(
            UUID.randomUUID().toString(),
            1234567890L,
            "Hi, it is your best friend, I want to join your party!",
            "Oleg",
            true,
            true
        );
    }

    public static AppUser createAppUser(boolean active) {
        return new AppUser(
            "testId",
            1234567890L,
            "Hi, it is your best friend, I want to join your party!",
            "Oleg",
            active,
            true
        );
    }

    public static AppUser createUnapprovedUser() {
        return new AppUser(
            UUID.randomUUID().toString(),
            1234567890L,
            "Hi, it is your best friend, I want to join your party!",
            "Oleg",
            false,
            false
        );
    }

    public static CreateNewUserRequest createNewUserRequest() {
        return new CreateNewUserRequest(
                1234567890L,
                "Oleg",
                "Hi, it is your best friend, I want to join your party!"
        );
    }

    public static IpAccess createIpAccess(String issuedFor) {
        return new IpAccess(
                UUID.randomUUID().toString(),
                "192.168.20.58",
                true,
                LocalDateTime.now(),
                issuedFor
        );
    }
}
