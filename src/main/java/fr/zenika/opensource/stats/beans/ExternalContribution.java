package fr.zenika.opensource.stats.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalContribution {

    private String memberId;
    private String memberName;
    private String memberHandle;
    private String projectName;
    private String projectFullName;
    private String projectUrl;
    private String prTitle;
    private String prUrl;
    private String prState;
    private String createdAt;
    private String source;
    private String year;
    private String month;

    public ExternalContribution() {
    }

    public ExternalContribution(String memberId, String memberName, String memberHandle,
                                String projectName, String projectFullName, String projectUrl,
                                String prTitle, String prUrl, String prState,
                                String createdAt, String source, String year, String month) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberHandle = memberHandle;
        this.projectName = projectName;
        this.projectFullName = projectFullName;
        this.projectUrl = projectUrl;
        this.prTitle = prTitle;
        this.prUrl = prUrl;
        this.prState = normalizeState(prState);
        this.createdAt = createdAt;
        this.source = source;
        this.year = year;
        this.month = month;
    }

    public static String normalizeState(String state) {
        if (state == null || state.isBlank()) {
            return "UNKNOWN";
        }
        String s = state.trim().toUpperCase();
        if ("OPENED".equals(s)) {
            return "OPEN";
        }
        return s;
    }

    public String getFormattedDate() {
        if (createdAt != null && !createdAt.isBlank()) {
            if (createdAt.contains("T")) {
                return createdAt.split("T")[0];
            }
            return createdAt;
        }
        if (year != null && month != null) {
            return year + " (" + month + ")";
        }
        return Optional.ofNullable(year).orElse("");
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberHandle() {
        return memberHandle;
    }

    public void setMemberHandle(String memberHandle) {
        this.memberHandle = memberHandle;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectFullName() {
        return projectFullName;
    }

    public void setProjectFullName(String projectFullName) {
        this.projectFullName = projectFullName;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public String getPrUrl() {
        return prUrl;
    }

    public void setPrUrl(String prUrl) {
        this.prUrl = prUrl;
    }

    public String getPrState() {
        return prState;
    }

    public void setPrState(String prState) {
        this.prState = normalizeState(prState);
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
