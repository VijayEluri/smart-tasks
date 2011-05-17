package com.wordpress.salaboy.smarttasks.formbuilder.api;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.example.ws_ht.api.TAttachment;
import org.example.ws_ht.api.TAttachmentInfo;
import org.example.ws_ht.api.TStatus;
import org.example.ws_ht.api.TTask;
import org.example.ws_ht.api.TTaskAbstract;
import org.example.ws_ht.api.wsdl.IllegalAccessFault;
import org.example.ws_ht.api.wsdl.IllegalArgumentFault;
import org.example.ws_ht.api.wsdl.IllegalStateFault;
import org.junit.Test;

import com.wordpress.salaboy.api.HumanTaskServiceOperations;
import com.wordpress.salaboy.smarttasks.formbuilder.api.ConnectionData;
import com.wordpress.salaboy.smarttasks.formbuilder.api.SmartTaskBuilder;
import com.wordpress.salaboy.smarttasks.formbuilder.api.TaskFormBuilder;
import com.wordpress.salaboy.smarttasks.formbuilder.api.TaskListBuilder;
import com.wordpress.salaboy.smarttasks.formbuilder.api.TaskListDataSet;
import com.wordpress.salaboy.smarttasks.formbuilder.configuration.BuilderConfiguration;
import com.wordpress.salaboy.smarttasks.formbuilder.configuration.BuilderConfigurationProvider;
import com.wordpress.salaboy.smarttasks.formbuilder.configuration.mock.MockConfigurationHandler;
import com.wordpress.salaboy.smarttasks.formbuilder.configuration.mock.MockHumanTaskClientConfiguration;
import com.wordpress.salaboy.smarttasks.formbuilder.configuration.mock.MockHumanTaskServiceOperations;
import com.wordpress.salaboy.smarttasks.metamodel.MetaTaskDecoratorBase;
import com.wordpress.salaboy.smarttasks.metamodel.MetaTaskDecoratorService;

public class TaskSupportUIHelperTest {

	@Test
	public void getTaskDetails() {
		MetaTaskDecoratorService.getInstance().registerDecorator("base",
				new MetaTaskDecoratorBase());
		File root = new File(Thread.currentThread().getContextClassLoader()
				.getResource(("TaskSupportUIHelperTest")).getFile());
		BuilderConfigurationProvider uIHelperConfigurationProvider = new BuilderConfigurationProvider(
				root);
		uIHelperConfigurationProvider
				.addUIHelperConfigurationUriHandler(new MockConfigurationHandler(
						new MockHumanTaskClientConfiguration() {

							@Override
							public HumanTaskServiceOperations getServiceOperationsImplementation() {
								return new MockHumanTaskServiceOperations() {
									@Override
									public String getTaskOriginatorType(
											String taskId) {
										return "mock";
									}

									@Override
									public TTask getTaskInfo(String identifier)
											throws IllegalArgumentFault {
										TTask task = new TTask();
										task.setId("1");
										task.setStatus(TStatus.IN_PROGRESS);
										return task;
									}

									@Override
									public List<TAttachmentInfo> getAttachmentInfos(
											String identifier)
											throws IllegalArgumentFault,
											IllegalStateFault,
											IllegalAccessFault {
										TAttachmentInfo att = new TAttachmentInfo();
										att.setName("attname");
										List<TAttachmentInfo> list = new ArrayList<TAttachmentInfo>();
										list.add(att);
										return list;
									}
									
									@Override
									public List<TAttachment> getAttachments(
											String identifier,
											String attachmentName)
											throws IllegalArgumentFault,
											IllegalStateFault,
											IllegalAccessFault {
										if ("attname".equals(attachmentName)) {
											TAttachment att = new TAttachment();
											att.setValue("Test");
											List<TAttachment> list = new ArrayList<TAttachment>();
											list.add(att);
											return list;
										}
										return null;
									}
									@Override
									public List<TTaskAbstract> getMyTaskAbstracts(
											String taskType,
											String genericHumanRole,
											String workQueue,
											List<TStatus> status,
											String whereClause,
											String orderByClause,
											String createdOnClause,
											Integer maxTasks,
											Integer fromTaskNumber)
											throws IllegalArgumentFault,
											IllegalStateFault {
										TTaskAbstract taskAbtrAbstract = new TTaskAbstract();
										taskAbtrAbstract.setId("1");
										List<TTaskAbstract> taskList = new ArrayList<TTaskAbstract>();
										taskList.add(taskAbtrAbstract);
										return taskList;
									}
								};
							}
						}));
		BuilderConfiguration configuration = uIHelperConfigurationProvider
				.createConfiguration();

		SmartTaskBuilder helper = new SmartTaskBuilder(configuration);

		// Connection
		ConnectionData connectionData = new ConnectionData();
		connectionData.setEntityId("Some_User");
		helper.connect(connectionData);
		TaskListBuilder listHelper = helper.getTaskListHelper("taskList1",
				"default");
		TaskListDataSet data = listHelper.getDataSet(0, 1);
		TaskFormBuilder taskHelper = helper.getTaskSupportHelper(
				data.getData()[0][0], "some", "salaboy");
		Map<String, String> details = taskHelper.getTaskInput();
		Assert.assertEquals("IN_PROGRESS", details.get("Status"));
	}
}
