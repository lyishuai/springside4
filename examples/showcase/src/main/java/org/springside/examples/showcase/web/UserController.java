package org.springside.examples.showcase.web;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springside.examples.showcase.entity.User;
import org.springside.examples.showcase.service.AccountService;

import com.google.common.collect.Maps;

@Controller
@RequestMapping(value = "/account/user")
public class UserController {

	@Autowired
	private AccountService accountService;

	private static Map<String, String> allStatus = Maps.newHashMap();

	static {
		allStatus.put("enabled", "有效");
		allStatus.put("disabled", "无效");
	}

	//特别设定多个ReuireRoles之间为Or关系，而不是默认的And.
	@RequiresRoles(value = { "Admin", "User" }, logical = Logical.OR)
	@RequestMapping(value = "")
	public String list(Model model) {
		List<User> users = accountService.getAllUser();
		model.addAttribute("users", users);
		model.addAttribute("allStatus", allStatus);
		return "account/userList";
	}

	@RequiresRoles("Admin")
	@RequestMapping(value = "update/{id}")
	public String updateForm(@PathVariable("id") Long id, Model model) {
		model.addAttribute("user", accountService.getUser(id));
		model.addAttribute("allStatus", allStatus);
		return "account/userForm";
	}

	@RequiresPermissions("user:edit")
	@RequestMapping(value = "save/{userId}")
	public String update(@Valid @ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
		accountService.saveUser(user);
		redirectAttributes.addFlashAttribute("message", "保存用户成功");
		return "redirect:/account/user";
	}

	@RequestMapping(value = "checkLoginName")
	@ResponseBody
	public String checkLoginName(@RequestParam("oldLoginName") String oldLoginName,
			@RequestParam("loginName") String loginName) {
		if (loginName.equals(oldLoginName)) {
			return "true";
		} else if (accountService.findUserByLoginName(loginName) == null) {
			return "true";
		}

		return "false";
	}

	/**
	 * 使用@ModelAttribute, 实现Struts2 Preparable二次部分绑定的效果,先根据form的id从数据库查出User对象,再把Form提交的内容绑定到该对象上。
	 * 因为仅update()方法的form中有id属性，因此本方法在该方法中执行.
	 */
	@ModelAttribute("user")
	public User getUser(@RequestParam(value = "id", required = false) Long id) {
		if (id != null) {
			return accountService.getUser(id);
		}
		return null;
	}
}
