package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "GENERAL-TEACHERS")
public class NotificationGeneralTeachers extends Notification {}
