package config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationMemory {

	@JsonProperty("LastUnpackSource")
	private String lastUnpackSource;

	@JsonProperty("LastUnpackTarget")
	private String lastUnpackTarget;

	@JsonProperty("LastRepackSource")
	private String lastRepackSource;

	@JsonProperty("LastRepackTarget")
	private String lastRepackTarget;

	@JsonProperty("UnityWebPlayerVersion")
	private String unityWebPlayerVersion;

	@JsonProperty("UnityEngineVersion")
	private String unityEngineVersion;

	@JsonCreator
	public ApplicationMemory() {
	}

	public String getLastUnpackSource() {
		return lastUnpackSource;
	}

	public ApplicationMemory setLastUnpackSource(String lastUnpackSource) {
		this.lastUnpackSource = lastUnpackSource;
		return this;
	}

	public String getLastUnpackTarget() {
		return lastUnpackTarget;
	}

	public ApplicationMemory setLastUnpackTarget(String lastUnpackTarget) {
		this.lastUnpackTarget = lastUnpackTarget;
		return this;
	}

	public String getLastRepackSource() {
		return lastRepackSource;
	}

	public ApplicationMemory setLastRepackSource(String lastRepackSource) {
		this.lastRepackSource = lastRepackSource;
		return this;
	}

	public String getLastRepackTarget() {
		return lastRepackTarget;
	}

	public ApplicationMemory setLastRepackTarget(String lastRepackTarget) {
		this.lastRepackTarget = lastRepackTarget;
		return this;
	}

	public String getUnityWebPlayerVersion() {
		return unityWebPlayerVersion != null ? unityWebPlayerVersion : "3.x.x";
	}

	public ApplicationMemory setUnityWebPlayerVersion(String unityWebPlayerVersion) {
		this.unityWebPlayerVersion = unityWebPlayerVersion;
		return this;
	}

	public String getUnityEngineVersion() {
		return unityEngineVersion != null ? unityEngineVersion : "3.5.6f4";
	}

	public ApplicationMemory setUnityEngineVersion(String unityEngineVersion) {
		this.unityEngineVersion = unityEngineVersion;
		return this;
	}
}
