package com.mmlm.useradmin.service;

import com.mmlm.useradmin.common.BusinessException;
import com.mmlm.useradmin.dto.task.TaskExportResponse;
import com.mmlm.useradmin.dto.task.TaskJobRequest;
import com.mmlm.useradmin.dto.task.TaskJobResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskJobService {

    private final CopyOnWriteArrayList<TaskJobResponse> jobs = new CopyOnWriteArrayList<TaskJobResponse>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @PostConstruct
    public void init() {
        if (!jobs.isEmpty()) {
            return;
        }
        TaskJobResponse sample = new TaskJobResponse();
        sample.setId(idGenerator.getAndIncrement());
        sample.setName("测试访客提醒任务");
        sample.setCronExpression("0 0/30 * * * ?");
        sample.setStatus("启用");
        sample.setRemark("默认测试数据，后续可替换为真实定时任务。");
        sample.setUpdatedAt(LocalDateTime.now());
        jobs.add(sample);
    }

    public List<TaskJobResponse> list() {
        List<TaskJobResponse> result = new ArrayList<TaskJobResponse>(jobs);
        result.sort(Comparator.comparing(TaskJobResponse::getUpdatedAt).reversed());
        return result;
    }

    public TaskJobResponse update(Long id, TaskJobRequest request) {
        TaskJobResponse job = findById(id);
        if (StringUtils.hasText(request.getName())) {
            job.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getCronExpression())) {
            job.setCronExpression(request.getCronExpression().trim());
        }
        if (StringUtils.hasText(request.getStatus())) {
            job.setStatus(request.getStatus().trim());
        }
        if (request.getRemark() != null) {
            job.setRemark(request.getRemark());
        }
        job.setUpdatedAt(LocalDateTime.now());
        return job;
    }

    public void delete(Long id) {
        TaskJobResponse job = findById(id);
        jobs.remove(job);
    }

    public TaskExportResponse export(Long id) {
        TaskJobResponse job = findById(id);
        TaskExportResponse response = new TaskExportResponse();
        response.setFileName("task-job-" + job.getId() + ".txt");
        response.setContent("任务名称：" + job.getName() + "\n"
                + "执行表达式：" + job.getCronExpression() + "\n"
                + "状态：" + job.getStatus() + "\n"
                + "备注：" + job.getRemark() + "\n"
                + "更新时间：" + job.getUpdatedAt());
        return response;
    }

    private TaskJobResponse findById(Long id) {
        return jobs.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new BusinessException("定时任务不存在"));
    }
}
