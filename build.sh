GHPAGES=../automata-gh/automata

# Build the HTML from the org file
# Tangle the cljs from the org file

DIR="org"
ORGSRC="~/emacs/elisp/org"
ORGINSTALL="${ORGSRC}/lisp/org-install.el"

emacs -Q --batch -l $ORGINSTALL \
    --eval "(progn
     (add-to-list 'load-path (expand-file-name \"${ORGSRC}/lisp/\"))
     (add-to-list 'load-path (expand-file-name \"${ORGSRC}/contrib/lisp/\"))
     (add-to-list 'load-path (expand-file-name \"../babel-clojurescript\"))
     (add-to-list 'load-path (expand-file-name \"~/git/clojure-mode\"))
     (add-to-list 'load-path (expand-file-name \"~/emacs/elisp/color-theme-6.6.0\"))

     (require 'org)
     (require 'org-exp)
     (require 'ob)
     (require 'ob-tangle)
     (require 'ob-clojurescript)
     (require 'clojure-mode)
     (require 'color-theme)
     
     (color-theme-select)
     (color-theme-classic)
     (find-file \"org/automata.org\")
     (org-mode)
     (org-babel-tangle)
     (kill-buffer))" 2>&1 | egrep "(Wrote|tangled)"


# Compile the cljs
lein cljsbuild once

# Move the site into the working dir for gh pages.
cp -v org/automata.html ${GHPAGES}/index.html
cp -v org/stylesheet.css ${GHPAGES}

cp -v site/automata.html site/stylesheet.css site/automata.js ${GHPAGES}/site

# Marginalia
lein marg -d ../automata-gh/automata -f marginalia.html -n "Andrew's Automata" -D "A project to learn ClojureScript" gen
