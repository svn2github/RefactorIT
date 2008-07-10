<?xml version="1.0" encoding="UTF-8"?>
<profile>
    <audit>
        <item active="false" id="unused_import" priority="LOW"/>
        <item active="false" id="unused_variable" priority="LOW">
            <skip catch_parameters="true" method_parameters="false"/>
        </item>
        <item active="false" id="empty_statement" priority="LOW"/>
        <item active="false" id="to_string" priority="LOW"/>
        <item active="false" id="boolean_compartion" priority="LOW"/>
        <item active="false" id="redundant_cast" priority="LOW"/>
        <item active="false" id="redundant_instanceof" priority="LOW"/>
        <item active="false" id="redundant_throws" priority="LOW"/>
        <item active="false" id="loose_nested_block" priority=""/>
        <item active="false" id="unused_assignment" priority="LOW"/>
        <item active="false" id="dangerous_catch" priority="LOW"/>
        <item active="false" id="shading" priority="LOW">
            <skip constructors="true" setters="true"/>
        </item>
        <item active="false" id="loop_variable" priority="LOW"/>
        <item active="false" id="equals_hashcode" priority="LOWL"/>
        <item active="false" id="switch_default" priority="LOW"/>
        <item active="false" id="switch_fallthrough" priority="LOW"/>
        <item active="false" id="parameter_reassignment" priority="LOW"/>
        <item active="false" id="abrupt_finally" priority="LOW"/>
        <item active="false" id="self_assignment" priority="HIGH"/>
        <item active="false" id="blockless" priority="LOW"/>
        <item active="false" id="static_field_accessed_by_reference" priority="LOW"/>
        <item active="false" id="static_method_accessed_by_reference" priority="LOW"/>
        <item active="true" id="static_candidate" priority="LOW"/>
        <item active="false" id="debug_code" priority="LOW"/>
    </audit>
</profile>
