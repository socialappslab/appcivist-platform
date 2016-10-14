package models;

import io.swagger.annotations.ApiModel;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Where;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(Include.NON_EMPTY)
@Where(clause="removed=false")
@ApiModel(value="MembershipRole", description="Role of an User within an Assembly or a Working Group")
public class MembershipRole extends Model {
	@Column(name="membership_membership_id")
	private Long membershipId;
	@Column(name="role_role_id")
	private Long roleId;
		
	public static Finder<Long, MembershipRole> find = new Finder<>(MembershipRole.class);

	public MembershipRole(Long membershipId, Long roleId) {
		super();
		this.membershipId = membershipId;
		this.roleId = roleId;
	}

	public MembershipRole() {
		super();
	}

	/*
	 * Getters and Setters
	 */

	public Long getMembershipId() {
		return membershipId;
	}

	public void setMembershipId(Long membershipId) {
		this.membershipId = membershipId;
	}

	public static MembershipRole create(MembershipRole membership) {
		membership.save();
		membership.refresh();
		return membership;
	}

	public static MembershipRole createObject(MembershipRole membership) {
		membership.save();
		return membership;
	}

	public static void delete(Long id) {
		find.ref(id).delete();
	}

	public static void update(Long id) {
		find.ref(id).update();
	}
}
