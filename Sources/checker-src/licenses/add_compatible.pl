#!/usr/bin/perl

# adds license name given as argument to every meta file
# found in the current directory. skips forbidden phrases.

use strict;

my $new_license = @ARGV[0];
my @meta_files = `find *.meta`;

if (!defined($new_license)) {
    print "no new license name given\n";
    exit;
}

for my $meta_file (@meta_files) {

    chomp($meta_file);

    # don't add for forbidden phrases
    if ($meta_file =~ /.*\-f.*/) {
	next;
    }

    # read file
    my $file;
    open(FILE_IN,"< $meta_file");
    for (<FILE_IN>) {
	my $line = $_;
	chomp($line);
	$file .= $line;
	# add license to isCompatible list
	if (/^isCompatible.*/) {
	    $file .= " $new_license\n";
	} else {
	    $file .= "\n";
	}
	# collapse two spaces
	$file =~ s/  / /;
    }
    close(FILE_IN);

    # backup meta file
    `mv $meta_file $meta_file.backup`;

    # write file
    open(FILE_OUT,"> $meta_file");
    print FILE_OUT $file;
    close(FILE_OUT);
}

