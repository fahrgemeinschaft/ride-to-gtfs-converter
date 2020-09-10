package com.ride2go.ridetogtfsconverter.model.item.routing.osm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;

@Getter
@XmlRootElement(name = "osm", namespace = "")
public class Osm {

	@XmlElement
	Node node;
}
