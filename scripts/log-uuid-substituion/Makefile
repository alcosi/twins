cfg ?= sample.conf
src ?= ../../logs/twins-core.log
sub:
	chmod +x logSubstitution.sh && cat ${src} | ./logSubstitution.sh ${cfg}


##########################################################################################
# EXAMPLES:                                                                              #
# make sub                                                  - run with defaults          #
# make sub cfg=domain.conf                                  - specify config file        #
# make sub src=../../twins-core-save.log                 - specify source log file    #
# make sub cfg=domain.conf src=../../twins-core-save.log - combined specify src & cfg #
#                                                                                        #
# TO ENABLE AUTOCOMPLETION FOR FILENAMES(if it dont work from box) TRY:                  #
# 1) add function from .bashrc_function to the "~/.bashrc"                				 #
# 2) run command "complete -F _make_filename_completion make"             				 #
##########################################################################################
