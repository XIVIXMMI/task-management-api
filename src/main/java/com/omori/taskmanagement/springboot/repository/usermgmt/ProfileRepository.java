package com.omori.taskmanagement.springboot.repository.usermgmt;

import com.omori.taskmanagement.springboot.model.usermgmt.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

}
