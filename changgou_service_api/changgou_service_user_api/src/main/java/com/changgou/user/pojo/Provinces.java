package com.changgou.user.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name="tb_provinces")
public class Provinces implements Serializable {

	@Id
	private String provinceId;//省份ID


	
	private String province;//省份名称
}
