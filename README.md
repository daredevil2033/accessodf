**AccessODF** is a Writer extension that helps authors evaluate and repair accessibility issues in OpenDocument Text.

# Introduction
AccessODF is an extension for LibreOffice Writer that helps authors evaluate and repair accessibility issues in OpenDocument Text. In other words, it helps authors solve issues that make their documents difficult or even impossible to read for people with disabilities. This includes issues such as:

- insufficient colour contrast between text and background,
- missing text alternatives for images and other objects (such text alternatives are necessary for blind users and other users of text-to-speech software),
- missing language identification for the document and for language changes inside the document (language information is needed for conversion into Braille and synthetic speech),
- the use of proper Heading styles instead of big bold text to identify headings (this enables document navigation based on headings),
- the use of proper tables instead of visually mimicking tables by using tabs or spaces,
- the compatibility of images with DAISY, the de-facto standard for digital talking books.
# Installation
- Download the extension.
- In OpenOffice.org or LibreOffice, go to Tools > Extensions, and browse to the OXT file to add the extension.
- After restarting the office suite, you will find a new "Accessibility evaluation" item in the Tools menu.
# Compatibility
AccessODF is compatible with the following versions of Apache OpenOffice and LibreOffice:

- Apache OpenOffice 3.4, 3.4.1;
- LibreOffice 3.3, 3.4, 3.5, 3.6 and 4.0;
- OpenOffice.org 3.3.

Older versions of OpenOffice are available at http://www.oldapps.com/openoffice.php. Older versions of LibreOffice are available at http://www.oldapps.com/libreoffice.php

# Using AccessODF
- Go to "Accessibility evaluation" in the Tools menu. This opens a taskpanel to the right of the editing area.
- Press the "Check" button at the bottom of the taskpanel and wait for the evaluation to complete. AccessODF will create a tree-like list of errors and warnings.
- Review each error and warning. For each issue, AccessODF displays its name, a description (what the problem is and why) and repair suggestions.
  - For some issues, the Repair button will become active; pressing this button will either repair the issue automatically or open the dialog where you can fix the issue. For other issues, you need to follow the instructions in the repair suggestions.
  - If AccessODF erroneously flagged something as an issue, press the Ignore button.
  - When you think you are ready, press the Check button again for a new evaluation. If all issues have been solved, AccessODF will display a success dialog.

**Warning**: not all accessibility issues can be detected automatically. For additional advice on accessibility, please consult the guidelines provided by the [Accessible Digital Office Document (ADOD) project](http://adod.idrc.ocad.ca/).
