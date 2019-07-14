package com.urdans.eetools.controllers;

import eecalcs.ConductorProperties;
import eecalcs.ConduitProperties;
import eecalcs.SystemACVoltages;
import eecalcs.VoltageDropCalculator;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestAPIController {
	@GetMapping("/api/calculatevoltageatload")
	public String calculateVoltageAtLoad(@RequestParam int acVoltageIndex, @RequestParam int wireSizeIndex,
            @RequestParam int conduitTypeIndex, @RequestParam int conductorTypeIndex, @RequestParam int sets,
            @RequestParam double wireLength, @RequestParam double loadCurrent, @RequestParam double pf) {

		double sourceVoltage = SystemACVoltages.getVoltages().get(acVoltageIndex).value;
		int phases = SystemACVoltages.getVoltages().get(acVoltageIndex).phases;
		String wireSize = ConductorProperties.wireSize[wireSizeIndex];
		ConduitProperties.ConduitMaterial conduitMaterial = ConduitProperties.getConduitMaterialPerIndex(conduitTypeIndex);
		ConductorProperties.ConductorMetal conductorMetal = ConductorProperties.getConductorMetalPerIndex(conductorTypeIndex);
//		if(conduitTypeIndex==0)
//			conduitMaterial = ConduitProperties.ConduitMaterial.PVC;
//		else if(conduitTypeIndex==1)
//			conduitMaterial = ConduitProperties.ConduitMaterial.ALUMINUM;
//		else
//			conduitMaterial = ConduitProperties.ConduitMaterial.STEEL;

//		ConductorProperties.ConductorMetal conductorMetal;
//
//		if(conductorTypeIndex == 0)
//			conductorMetal = ConductorProperties.ConductorMetal.COPPER;
//		else
//			conductorMetal = ConductorProperties.ConductorMetal.ALUMINUM;

		VoltageDropCalculator vd = new VoltageDropCalculator(sourceVoltage, phases, wireSize, conduitMaterial,
				conductorMetal, sets, wireLength, loadCurrent, pf, false,  ConductorProperties.CopperCoating.COATED);
		JSONObject result = new JSONObject();
		JSONObject msg = new JSONObject();

		if(vd.hasErrors()){
			msg.put("isError", true);
			String msgErrors = "";
			for(VoltageDropCalculator.Error error: vd.Errors) {
				msgErrors = msgErrors + error.getErrorMessage() + "\n";
			}
			msg.put("message", msgErrors);
			result.put("voltageAtLoad", -1);
			result.put("voltageDropVolts", -1);
			result.put("voltageDropPercentage", -1);
			result.put("messageContainer", msg);
			return result.toString();
		}

		msg.put("message", "Voltage drop has been calculated");
		msg.put("isError", false);
		result.put("voltageAtLoad", vd.getVoltageAtLoad());
		result.put("voltageDropVolts", vd.getVoltageDropVolts());
		result.put("voltageDropPercentage", vd.getVoltageDropPercentage());
		result.put("messageContainer", msg);
		return result.toString();
	}

	@GetMapping("/api/conductorsizingpervoltagedrop")
	public String calculateConductorPerVoltageDrop(@RequestParam int acVoltageIndex, @RequestParam double maxVoltageDropValue,
			@RequestParam int conduitTypeIndex, @RequestParam int conductorTypeIndex, @RequestParam int sets,
			@RequestParam double wireLength, @RequestParam double loadCurrent, @RequestParam double pf) {

		double sourceVoltage = SystemACVoltages.getVoltages().get(acVoltageIndex).value;
		int phases = SystemACVoltages.getVoltages().get(acVoltageIndex).phases;
		ConduitProperties.ConduitMaterial conduitMaterial = ConduitProperties.getConduitMaterialPerIndex(conduitTypeIndex);
		ConductorProperties.ConductorMetal conductorMetal = ConductorProperties.getConductorMetalPerIndex(conductorTypeIndex);
		VoltageDropCalculator vd = new VoltageDropCalculator(sourceVoltage, phases, "1/0", conduitMaterial,
				conductorMetal, sets, wireLength, loadCurrent, pf, false,  ConductorProperties.CopperCoating.COATED);
		vd.setMaxVDropPercent(maxVoltageDropValue);

		JSONObject result = new JSONObject();
		JSONObject msg = new JSONObject();

		if(vd.hasErrors()){
			msg.put("isError", true);
			String msgErrors = "";
			for(VoltageDropCalculator.Error error: vd.Errors) {
				msgErrors = msgErrors + error.getErrorMessage() + "\n";
			}
			msg.put("message", msgErrors);
			result.put("conductorSize", "");
			result.put("voltageDropPercentage", -1);
			result.put("maximumLength", -1);
			result.put("messageContainer", msg);
			return result.toString();
		}

		msg.put("message", "Conductor size has been calculated");
		msg.put("isError", false);
		result.put("conductorSize", ConductorProperties.getFullWireSizeName(vd.getWireSize()));
		result.put("voltageDropPercentage", vd.getVoltageDropPercentage());
		result.put("maximumLength", vd.getMaxLength());
		result.put("messageContainer", msg);
		return result.toString();
	}
/*	//example: http://localhost:8080/api/updatesubject data about subject in a json block
	@PutMapping("/api/updatesubject")
	public boolean updateSubjectById(@RequestBody Subject subjectToEdit, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return false;

		int id = subjectToEdit.getId();

		if (id == 0) return false;

		Subject subject = subjectDao.findById(id).orElse(null);

		if (subject == null) return false; //subject to edit not found

		subject.setName(subjectToEdit.getName());
		subjectDao.save(subject);
		return true;
	}

	@PostMapping("/api/addsubject")
	public int addSubject(@RequestBody Subject newSubject, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return -1;

		if (newSubject.getName() == null || newSubject.getName().length() < 3) return -1;

		//trim the subject name and make it uppercase
		newSubject.setName(newSubject.getName().trim().toUpperCase());
		//check that the subject doesn't exist already
		int count = subjectDao.countByName(newSubject.getName());

		if (count > 0) return -1;

		Subject subject = subjectDao.save(newSubject);

		return subject.getId();
	}

	@DeleteMapping("/api/deletesubject")
	public boolean deleteSubject(@RequestBody Subject subjectToDelete, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return false;

		Subject subject = subjectDao.findById(subjectToDelete.getId()).orElse(null);

		if (subject == null) return false;

		try {
			subjectDao.delete(subject);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	@PostMapping("/api/addgroup")
	public int addGroup(@RequestBody Group newGroup, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return 0;

		if (newGroup.getName().length() < 1) return 0;

		return groupDao.save(newGroup).getId();
	}

	@PutMapping("/api/updategroup")
	public boolean updateGroupById(@RequestBody Group groupToEdit, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return false;

		int id = groupToEdit.getId();

		if (id == 0) return false;

		Group group = groupDao.findById(id).orElse(null);

		if (group == null) return false; //group to edit not found

		group.setName(groupToEdit.getName());
		groupDao.save(group);
		return true;
	}

	@DeleteMapping("/api/deletegroup")
	public MessageContainer deleteGroup(@RequestBody Group groupToDelete, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		Group group = groupDao.findById(groupToDelete.getId()).orElse(null);

		if (group == null) return new MessageContainer("Group not found!", true);

		try {
			groupDao.delete(group);
		} catch (Exception e) {
			return new MessageContainer("Can not delete this group while it's in use!", true);
		}
		return new MessageContainer("Group name deleted!", false);
	}

	@PostMapping("/api/addgrouping")
	public MessageContainer addGrouping(@RequestBody GroupingForm newGrouping, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		if (newGrouping.group_id == 0 || newGrouping.subject_id == 0 || newGrouping.teacher_id == 0)
			return new MessageContainer("Invalid " +
					"group/subject/teacher identifiction!", true);

		//check if this group already has this subject
		int count = groupingDao.countBySubjectIdAndGroupId(newGrouping.subject_id, newGrouping.group_id);

		if (count > 0) return new MessageContainer("Subject is already in the group!", true);

		Group group = groupDao.findById(newGrouping.group_id).orElse(null);

		if (group == null) return new MessageContainer("Group not found!", true);

		Subject subject = subjectDao.findById(newGrouping.subject_id).orElse(null);

		if (subject == null) return new MessageContainer("Subject not found!", true);

		Teacher teacher = teacherDao.findById(newGrouping.teacher_id).orElse(null);

		if (teacher == null) return new MessageContainer("Teacher not found!", true);

		Grouping grouping = new Grouping(group, subject, teacher);
		int id = groupingDao.save(grouping).getId();

		MessageContainer messageContainer = new MessageContainer("Subject-teacher pairing added!", false);
		messageContainer.id = id;
		return messageContainer;
	}

	@DeleteMapping("/api/deletegrouping")
	public MessageContainer deleteGrouping(@RequestBody GroupingForm groupingToDelete, HttpServletRequest request) {

		if (!registrarController.registrarLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		Grouping grouping = groupingDao.findById(groupingToDelete.group_id).orElse(null);

		if (grouping == null) return new MessageContainer("Subject-teacher pairing not found!", true);

		try {
			groupingDao.delete(grouping);
		} catch (Exception e) {
			return new MessageContainer("Can not remove this subject-teacher pairing while it's in use!", true);
		}
		return new MessageContainer("Subject-teacher pairing removed!", false);
	}

	@PostMapping("/api/addevaluation")
	public MessageContainer addEvaluation(@RequestBody ScheduleForm newSchedule, HttpServletRequest request) {

		if (!teacherController.teacherLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		if (newSchedule.groupingId == 0 || newSchedule.date == "" || newSchedule.description.length() <= 2 || newSchedule.percentage <= 0)
			return new MessageContainer("Required data: Description lenght > 2; valid date; percentage > 0.", true);

		//check if grouping exists
		Grouping grouping = groupingDao.findById(newSchedule.groupingId).orElse(null);
		if (grouping == null) return new MessageContainer("Invalid grouping", true);

		if (scheduleDao.getPercentageSumByGroupingId(newSchedule.groupingId) + newSchedule.percentage > 100.0)
			return new MessageContainer("Percentage sum can't be greater that 100% !", true);

		Schedule schedule = new Schedule(newSchedule.extractDate(), newSchedule.percentage, newSchedule.description, grouping);
		int id = scheduleDao.save(schedule).getId();
		MessageContainer messageContainer = new MessageContainer("Evaluation added!", false);
		messageContainer.id = id;
		return messageContainer;
	}

	@PutMapping("/api/updateevaluation")
	public MessageContainer updateEvaluation(@RequestBody ScheduleForm newSchedule, HttpServletRequest request) {

		if (!teacherController.teacherLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		if (newSchedule.groupingId == 0 || newSchedule.date == "" || newSchedule.description.length() <= 2 || newSchedule.percentage <= 0)
			return new MessageContainer("Required data: Description lenght > 2; valid date; percentage > 0.", true);

		if (newSchedule.scheduleId == 0) return new MessageContainer("Missing schedule Id !", true);

		//check if grouping exists
		Grouping grouping = groupingDao.findById(newSchedule.groupingId).orElse(null);

		if (grouping == null) return new MessageContainer("Invalid grouping", true);

		if (scheduleDao.getPercentageSumByGroupingIdExcluding(newSchedule.groupingId, newSchedule.scheduleId) +
				newSchedule.percentage > 100.0)
			return new MessageContainer("Percentage sum can't be greater that 100% !", true);

		Schedule schedule = scheduleDao.findById(newSchedule.scheduleId).orElse(null);
		if (schedule == null) return new MessageContainer("Schedule not found!", true);

		schedule.setDescription(newSchedule.description);
		schedule.setDate(newSchedule.extractDate());
		schedule.setPercentage(newSchedule.percentage);
		scheduleDao.save(schedule);
		MessageContainer messageContainer = new MessageContainer("Evaluation saved!", false);
		messageContainer.id = 0;
		return messageContainer;
	}

	@DeleteMapping("/api/deleteevaluation")
	public MessageContainer deleteEvaluation(@RequestBody ScheduleForm newSchedule, HttpServletRequest request) {

		if (!teacherController.teacherLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		Schedule schedule = scheduleDao.findById(newSchedule.scheduleId).orElse(null);

		if (schedule == null) return new MessageContainer("Schedule not found!", true);

		try {
			scheduleDao.delete(schedule);
		} catch (Exception e) {
			return new MessageContainer("Can not remove this evaluation while it's in use!", true);
		}
		return new MessageContainer("Evaluation removed!", false);
	}

	@GetMapping("/api/getgrades")
	public GradesForm getStudentGrades(@RequestParam(value = "groupingid", defaultValue = "0") int groupingId,
	                                   @RequestParam(value = "studentid", defaultValue = "0") int studentId,
	                                   HttpServletRequest request) {

		if (!teacherController.teacherLoggedIn(request) && !studentController.studentLoggedIn(request)) return null;

		if (groupingId == 0 || studentId == 0) return null;

		if (userSession.getUserType() == UserType.STUDENT && studentId != userSession.getUserId()) return null;

		return new GradesForm(scheduleDao.getGradesByGroupingAndStudent(groupingId, studentId));
	}

	@PostMapping("/api/setgrades")
	public MessageContainer setStudentGrades(@RequestBody StudentGradesForm studentGradesForm, HttpServletRequest request) {

		if (!teacherController.teacherLoggedIn(request)) return new MessageContainer("Unauthorized!", true);

		Student student = studentDao.findById(studentGradesForm.studentId).orElse(null);

		if (student == null)
			return new MessageContainer("Student id: " + studentGradesForm.studentId + " not found!. Nothing saved.", true);

		if (studentGradesForm.gradeList.size() == 0) return new MessageContainer("Nothing to save!", true);

		for (StudentGrade grade : studentGradesForm.gradeList) {

			Schedule schedule = scheduleDao.findById(grade.scheduleId).orElse(null);

			if (schedule == null) return new MessageContainer("Schedule id: " + grade.scheduleId + " not found!. Some grades might " +
					"have been saved.", true);

			if (grade.gradeId == 0 && grade.gradeValue != "") {
				gradeDao.save(new Grade(schedule, student, Double.parseDouble(grade.gradeValue)));
			} else if (grade.gradeId != 0) { //existing grade
				Grade existingGrade = gradeDao.findById(grade.gradeId).orElse(null);

				if (existingGrade == null) return new MessageContainer("Grade id:" + grade.gradeId +
						" not found!. Some grades might have been saved.", true);

				if (grade.gradeValue == "") {//grade must be deleted
					try {
						gradeDao.delete(existingGrade);
					} catch (Exception e) {
						return new MessageContainer("Can not delete the existing grade id: " + grade.gradeId + " !", true);
					}
				} else {//potentially modified value
					existingGrade.setValue(Double.parseDouble(grade.gradeValue));
					gradeDao.save(existingGrade);
				}
			}
		}
		MessageContainer messageContainer = new MessageContainer("Student grades saved!", false);
		messageContainer.id = 0;
		return messageContainer;
	}*/
}

