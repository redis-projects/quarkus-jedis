package org.acme.redis.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RegisterForReflection
public class Student {

    // TODO :: Make the data structure more representative of data types
    // TODO :: Give this stricture some nested values so it doesnt represent a simple hash
    private String title;
    private String firstName;
    private String lastName;
    private String gender;
    private String birth;
    private String email;
    private String city;
    private String company;
    private String preferredColour;
    private String streetAddress;
    private String plannedCareer;
    private String university;
    private int age;

}
