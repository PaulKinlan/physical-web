default:
	# Upload the lamp.
	s3cmd put --recursive . s3://z3.ca/1/
	# Make it accessible.
	s3cmd setacl --acl-public --recursive s3://z3.ca/1/
