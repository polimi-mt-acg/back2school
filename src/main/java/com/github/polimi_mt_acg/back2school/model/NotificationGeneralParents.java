package com.github.polimi_mt_acg.back2school.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "GENERAL-PARENTS")
public class NotificationGeneralParents extends Notification {}
