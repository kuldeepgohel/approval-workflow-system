package com.kd.aws.service;

import com.kd.aws.dto.request.CreateApprovalWorkflowRequest;
import com.kd.aws.dto.request.UpdateApprovalWorkflowRequest;
import com.kd.aws.dto.request.WorkflowStepRequest;
import com.kd.aws.dto.response.ApprovalWorkflowResponse;
import com.kd.aws.entity.ApprovalWorkflow;
import com.kd.aws.entity.Department;
import com.kd.aws.entity.Role;
import com.kd.aws.entity.WorkflowStep;
import com.kd.aws.mapper.ApprovalWorkflowMapper;
import com.kd.aws.repository.ApprovalWorkflowRepository;
import com.kd.aws.repository.DepartmentRepository;
import com.kd.aws.repository.RoleRepository;
import com.kd.aws.repository.WorkflowStepRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ApprovalWorkflowServiceImpl implements ApprovalWorkflowService{

    private ApprovalWorkflowRepository approvalWorkflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final ApprovalWorkflowMapper approvalWorkflowMapper;

    public ApprovalWorkflowServiceImpl(
            ApprovalWorkflowRepository approvalWorkflowRepository,
            WorkflowStepRepository workflowStepRepository,
            DepartmentRepository departmentRepository,
            RoleRepository roleRepository,
            ApprovalWorkflowMapper approvalWorkflowMapper
    ) {
        this.approvalWorkflowRepository = approvalWorkflowRepository;
        this.workflowStepRepository = workflowStepRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.approvalWorkflowMapper = approvalWorkflowMapper;
    }


    @Override
    @Transactional
    public ApprovalWorkflowResponse createWorkFlow(CreateApprovalWorkflowRequest request){

//        1.validate workflow steps
        validateWorkflowSteps(request.getSteps());

//        2.find department
        Department department = departmentRepository.findById(
                request.getDepartmentId()
        ).orElseThrow(() ->
                new EntityNotFoundException(
                        "Department not found with id:"
                         + request.getDepartmentId()
                )
        );

 // 3. Check whether department already has an active workflow
        approvalWorkflowRepository
                .findByDepartmentIdAndActiveTrue(department.getId())
                .ifPresent(existingWorkflow -> {
                    throw new IllegalStateException(
                            "Active workflow already exists for department: "
                            + department.getName()
                    );
                });
//        4. Create workflow entity
        ApprovalWorkflow workflow = new ApprovalWorkflow();

        workflow.setName(request.getName());
        workflow.setDepartment(department);
        workflow.setActive(true);

//        5. Save workflow first
        workflow = approvalWorkflowRepository.save(workflow);

//        6. Create workflow steps
        for (WorkflowStepRequest stepRequest: request.getSteps()) {

            Role role = roleRepository.findById(
                    stepRequest.getRoleId()
            ).orElseThrow(() ->
                    new EntityNotFoundException(
                            "Role not found with id:"
                            + stepRequest.getRoleId()
                    )
            );
            WorkflowStep workflowStep = new WorkflowStep();

            workflowStep.setWorkflow(workflow);
            workflowStep.setLevel(stepRequest.getLevel());
            workflowStep.setRole(role);

            workflowStepRepository.save(workflowStep);
        }

//       7.Fetch steps in correct level order
        List<WorkflowStep> steps =
                workflowStepRepository
                        .findByWorkflowIdOrderByLevelAsc(workflow.getId());

//       8.Convert entity -> response DTO
        return approvalWorkflowMapper.toResponse(
                workflow,
                steps
        );
    }

    @Override
    public List<ApprovalWorkflowResponse> getAllWorkFlows() {
        return approvalWorkflowRepository.findAll()
                .stream()
                .map(workflow -> {
                    List<WorkflowStep> steps =
                            workflowStepRepository.findByWorkflowIdOrderByLevelAsc(
                                    workflow.getId()
                            );
                    return approvalWorkflowMapper.toResponse(
                            workflow,
                            steps
                    );
                })
                .toList();
    }

    @Override
    public ApprovalWorkflowResponse getWorkflowById(Long id) {
        ApprovalWorkflow workflow = approvalWorkflowRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Approval workflow not found with id:"
                                + id
                        ));
        List<WorkflowStep> steps = workflowStepRepository.findByWorkflowIdOrderByLevelAsc(id);

        return approvalWorkflowMapper.toResponse(
                workflow,
                steps
        );
    }

    @Override
    @Transactional
    public ApprovalWorkflowResponse updateWorkflow(
            Long id,
            UpdateApprovalWorkflowRequest request
    ) {
//        1.validate new steps
        validateWorkflowSteps(request.getSteps());

//        2.Find existing workflow
        ApprovalWorkflow workflow = approvalWorkflowRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Approval workflow not found with id:"
                                + id
                        )
                );
//        3.Update  workflow name
        workflow.setName(request.getName());
        approvalWorkflowRepository.save(workflow);

//        4.Remove old steps
        List<WorkflowStep> existingSteps =
                workflowStepRepository.findByWorkflowIdOrderByLevelAsc(id);
        workflowStepRepository.deleteAll(existingSteps);

//        5. Create New Steps
        for(WorkflowStepRequest stepRequest: request.getSteps()){
            Role role = roleRepository.findById(
                    stepRequest.getRoleId()
            ).orElseThrow(() ->
                    new EntityNotFoundException(
                            "Role not found with id: "
                                    + stepRequest.getRoleId()
                    )
            );

            WorkflowStep workflowStep = new WorkflowStep();

            workflowStep.setWorkflow(workflow);
            workflowStep.setLevel(stepRequest.getLevel());
            workflowStep.setRole(role);

            workflowStepRepository.save(workflowStep);
        }

//        6.Fetch updated steps
        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowIdOrderByLevelAsc(id);

        return approvalWorkflowMapper.toResponse(
                workflow,
                steps
        );
    }

    @Override
    @Transactional
    public void deleteWorkflow(Long id) {
        ApprovalWorkflow workflow =
                approvalWorkflowRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Approval WorkFlow not found with id"
                                        + id
                                )
                        );
        List<WorkflowStep> steps =
                workflowStepRepository.findByWorkflowIdOrderByLevelAsc(id);

//        delete steps first before they reference workflow
        workflowStepRepository.deleteAll(steps);

        approvalWorkflowRepository.delete(workflow);
    }

    private void validateWorkflowSteps(
            List<WorkflowStepRequest> steps
    ) {
        Set<Integer> levels = new HashSet<>();

        for(WorkflowStepRequest step: steps){
//            Duplicate level check
            if(!levels.add(step.getLevel())){
                throw new IllegalArgumentException(
                        "Duplicate workflow level: "
                                + step.getLevel()
                );
            }
        }
//      Check levels are sequential starting from 1
        for (int i = 1; i < steps.size(); i++) {
            if(!levels.contains(i)){
                throw new IllegalArgumentException(
                        "Workflow levels must be sequential starting from 1"
                );
            }
        }
    }
}
