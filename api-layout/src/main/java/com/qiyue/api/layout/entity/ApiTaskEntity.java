package com.qiyue.api.layout.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "api_task", uniqueConstraints = @UniqueConstraint(columnNames = {"task_id"}))
public class ApiTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "task_desc")
    private String taskDesc;

    @Column(name = "task_info")
    private String taskInfo;
}
