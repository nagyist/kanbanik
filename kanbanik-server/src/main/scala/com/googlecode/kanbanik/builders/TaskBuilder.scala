package com.googlecode.kanbanik.builders
import org.bson.types.ObjectId
import com.googlecode.kanbanik.dto.ClassOfService
import com.googlecode.kanbanik.dto.TaskDto
import com.googlecode.kanbanik.model.TaskScala
import com.googlecode.kanbanik.model.WorkflowitemScala
import com.googlecode.kanbanik.commands.TaskManipulation
import com.googlecode.kanbanik.model.ProjectScala

class TaskBuilder extends TaskManipulation {

  def buildDto(task: TaskScala): TaskDto = {
    val workflowitemBuilder = new WorkflowitemBuilder
    
    val dto = new TaskDto
    dto.setId(task.id.get.toString())
    dto.setName(task.name)
    dto.setDescription(task.description)
    dto.setClassOfService(ClassOfService.fromId(task.classOfService))
    dto.setWorkflowitem(workflowitemBuilder.buildDtoNonRecursive(task.workflowitem))
    dto.setTicketId(task.ticketId)
    val project: ProjectScala = findProjectForTask(task).getOrElse(throw new IllegalStateException("No project for task: '" + task.id + "' found!"))
    dto.setProject(projectBuilder.buildShallowDto(project))
    dto
  }

  def buildEntity(taskDto: TaskDto): TaskScala = {
    new TaskScala(
      determineId(taskDto),
      taskDto.getName(),
      taskDto.getDescription(),
      taskDto.getClassOfService().getId(),
      determineTicketId(taskDto),
      WorkflowitemScala.byId(new ObjectId(taskDto.getWorkflowitem().getId())));
  }
  
  private def determineId(taskDto: TaskDto): Option[ObjectId]= {
    if (taskDto.getId() != null) {
    	return Some(new ObjectId(taskDto.getId()))  
    }
    
    None
    
  }
  
  private def determineTicketId(taskDto: TaskDto): String = {
    if (taskDto.getId() == null) {
      return generateUniqueTicketId()
    }
    
    if (taskDto.getId() != null && taskDto.getTicketId() == null) {
      throw new IllegalStateException("The task " + taskDto.getId() + " has not set a ticket id!" )
    }
    
    taskDto.getTicketId()
  }
  
  private[builders] def projectBuilder = new ProjectBuilder
}