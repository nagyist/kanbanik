package com.googlecode.kanbanik.client.components.board;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.googlecode.kanbanik.client.KanbanikResources;
import com.googlecode.kanbanik.client.Modules;
import com.googlecode.kanbanik.client.api.DtoFactory;
import com.googlecode.kanbanik.client.api.Dtos;
import com.googlecode.kanbanik.client.components.task.TaskAddingComponent;
import com.googlecode.kanbanik.client.messaging.Message;
import com.googlecode.kanbanik.client.messaging.MessageBus;
import com.googlecode.kanbanik.client.messaging.MessageListener;
import com.googlecode.kanbanik.client.messaging.messages.project.GetAllProjectsRequestMessage;
import com.googlecode.kanbanik.client.messaging.messages.project.GetAllProjectsResponseMessage;
import com.googlecode.kanbanik.client.modules.lifecyclelisteners.ModulesLifecycleListener;
import com.googlecode.kanbanik.client.modules.lifecyclelisteners.ModulesLyfecycleListenerHandler;

import java.util.ArrayList;
import java.util.List;

public class ProjectHeader extends Composite implements ModulesLifecycleListener, MessageListener<Dtos.ProjectDto> {

    interface MyUiBinder extends UiBinder<Widget, ProjectHeader> {
	}

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	@UiField
	Label projectName;
	
	@UiField
	PushButton addButton;

    private Dtos.BoardDto board;

    private Dtos.ProjectDto project;

	public ProjectHeader(Dtos.BoardDto board, Dtos.ProjectDto project) {
        this.board = board;
        this.project = project;
        initWidget(uiBinder.createAndBindUi(this));

		projectName.setText(project.getName());
		Dtos.WorkflowitemDto rootDto = board.getWorkflow().getWorkflowitems().size() > 0 ? board.getWorkflow().getWorkflowitems().get(0) : null;
		
		if (rootDto != null) {
			addButton.getUpFace().setImage(new Image(KanbanikResources.INSTANCE.addButtonImage()));	
		} else {
			// the board has no workflow, disable add button
			addButton.setEnabled(false);
			addButton.setTitle("It is not possible to add a task to a board when the board has no workflow.");
			addButton.getUpFace().setImage(new Image(KanbanikResources.INSTANCE.addDisabledButtonImage()));
		}
		
		new TaskAddingComponent(project, getInputQueue(rootDto), addButton, board);

        MessageBus.registerListener(GetAllProjectsRequestMessage.class, this);

        new ModulesLyfecycleListenerHandler(Modules.BOARDS, this);
	}

	
	private Dtos.WorkflowitemDto getInputQueue(Dtos.WorkflowitemDto root) {
		if (root == null) {
			return null;
		}
		
		
		if (root.getNestedWorkflow().getWorkflowitems().size() == 0) {
			return root;
		} else {
			return getInputQueue(root.getNestedWorkflow().getWorkflowitems().get(0));
		}
	}

    @Override
    public void activated() {
        if (!MessageBus.listens(GetAllProjectsRequestMessage.class, this)) {
            MessageBus.registerListener(GetAllProjectsRequestMessage.class, this);
        }
    }

    @Override
    public void deactivated() {
        MessageBus.unregisterListener(GetAllProjectsRequestMessage.class, this);
    }

    @Override
    public void messageArrived(Message<Dtos.ProjectDto> message) {
        Dtos.BoardWithProjectsDto boardWithProjectsDto = DtoFactory.boardWithProjectsDto();
        boardWithProjectsDto.setBoard(board);
        List<Dtos.ProjectDto> projects = new ArrayList<Dtos.ProjectDto>();
        projects.add(project);
        boardWithProjectsDto.setProjectsOnBoard(DtoFactory.projectsDto(projects));

        MessageBus.sendMessage(new GetAllProjectsResponseMessage(boardWithProjectsDto, this));
    }
	
}
