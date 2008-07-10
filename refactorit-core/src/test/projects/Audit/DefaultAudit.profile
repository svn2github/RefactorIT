<?xml version="1.0" encoding="ISO-8859-1"?>

<profile>
  <audit>
    <!-- J2SE 5.0 features -->
    <item id="forin" active="true" priority="NORMAL"/>
    <item id="generics" active="true" priority="LOW"/>
    <item id="redundant_boxing" active="true" priority="LOW"/>
    <item id="redundant_unboxing" active="true" priority="LOW"/>

    <!-- exception usage -->
    <item id="dangerous_catch" active="true" priority="HIGH"/>
    <item id="dangerous_throw" active="true" priority="HIGH"/>
    <item id="redundant_throws" active="true" priority="HIGH"/>
    <item id="aborted_finally" active="true" priority="NORMAL"/>

  	<!-- inheritance -->
    <item id="abstract_subclass" active="true" priority="NORMAL"/>
    <item id="abstract_override" active="true" priority="NORMAL"/>
    <item id="hidden_field" active="true" priority="NORMAL"/>
    <item id="hidden_method" active="true" priority="NORMAL"/>

  	<!-- modifier usage -->
    <item id="modifier_order" active="true" priority="LOW"/>
    <item id="pseudo_abstract" active="true" priority="LOW"/>
    <item id="redundant_modifiers" active="true" priority="LOW"/>
    <item id="static_candidate" active="true" priority="LOW"/>
    <item id="finalize_methods" active="true" priority="LOW"/>
    <item id="finalize_locals" active="true" priority="LOW"/>
    <item id="constant_field" active="true" priority="LOW">
      <skip upper_case_names="true"/>
    </item>

    <!-- complexity -->
    <item id="method_calls_method" active="true" priority="LOW">
      <options find="1"/> 
      <skip delegation="true" proxy="true" super="true"/>
    </item>
    <item id="method_body_length" active="true" priority="LOW">
      <options skip_getters="true" allow_min_value="true" allow_max_value="true"
          min_value="2" max_value="20"/>
    </item>
    <item id="LawOfDemeter" active="true" priority="LOW">
        
    <!-- serialization -->
    <item id="not_serializable_super" active="true" priority="HIGH">
      <skip noarg_constr="false"/>
    </item>
    <item id="serializable_fields" active="true" priority="NORMAL">
      <skip no_instance="true" only_transient="true" alerts_with_array="false"/>
    </item>
    <item id="serial_version_uid" active="true" priority="NORMAL"/>

  	<!-- unused code -->
    <item id="unused_import" active="true" priority="LOW"/>
    <item id="unused_variable" active="true" priority="NORMAL">
      <skip method_parameters="false" catch_parameters="true"/>
    </item>
    <item id="empty_statement" active="true" priority="LOW"/>

  	<!-- redundant code -->
    <item id="to_string" active="true" priority="LOW"/>
    <item id="boolean_comparison" active="true" priority="NORMAL"/>
    <item id="redundant_cast" active="true" priority="LOW">
      <skip bitwise_primitives="false"/>
    </item>
    <item id="redundant_instanceof" active="true" priority="LOW"/>
    <item id="nested_block" active="true" priority="LOW"/>
    <item id="unused_assignment" active="true" priority="NORMAL"/>

  	<!-- dangerous code -->
    <item id="shading" active="true" priority="NORMAL">
      <skip constructors="true" setters="true"/>
    </item>
    <item id="loop_variable" active="true" priority="NORMAL"/>
    <item id="equals_hashcode" active="true" priority="NORMAL"/>
    <item id="equals_on_diff_types" active="true" priority="HIGH">
      <skip same_branch="true"/>
    </item>
    <item id="str_equal_compare" active="true" priority="HIGH"/>
    <item id="switch_default" active="true" priority="HIGH"/>
    <item id="switch_case" active="true" priority="NORMAL"/>
    <item id="parameter_assignment" active="true" priority="NORMAL"/>
    <item id="parameter_order" active="true" priority="LOW">
      <options precision="65"/>
    </item>
    <item id="float_equal_compare" active="true" priority="HIGH"/>
    <item id="int_division" active="true" priority="HIGH"/>
    <item id="self_assignment" active="true" priority="HIGH"/>
    <item id="missing_block" active="true" priority="NORMAL"/>
    <item id="empty_blocks" active="true" priority="LOW">
      <skip with_comments="true"/>
    </item>
    <item id="nonstatic_reference" active="true" priority="NORMAL"/>
    <item id="loop_condition" active="true" priority="HIGH"/>
    <item id="string_concat_order" active="true" priority="LOW"/>
    <item id="lost_override" active="true" priority="LOW"/>
	<item id="possible_npe" active="true" priority="NORMAL"/>
	
  	<!-- misc -->
    <item id="debug_code" active="true" priority="NORMAL"/>
    <item id="numeric_literals" active="true" priority="LOW">
      <options accepted="-1;0;1;" skip_collections="true"/>
    </item>

    <!-- other -->
    <item id="PMD_" active="false" priority="NORMAL"/>
  </audit>
</profile>
