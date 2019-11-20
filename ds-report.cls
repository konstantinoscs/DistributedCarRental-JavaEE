\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{ds-report}[2019/10/10 v0.1.0 Report for Distributed Systems]
\ProcessOptions\relax
\LoadClass[a4paper]{article} % \@@ptsize, 
\RequirePackage[T1]{fontenc}
\RequirePackage[utf8]{inputenc}
\RequirePackage{microtype}
\RequirePackage{a4wide}
\RequirePackage{enumitem}
\RequirePackage[english]{babel}
\RequirePackage[pdftex]{color,graphicx}

\newcommand{\@assignment}{}
\newcommand{\assignment}[1]{\renewcommand{\@assignment}{{#1}}}
\newcommand{\@authorOne}{\color{red}\textbf{fill in name}}
\newcommand{\authorOne}[1]{\renewcommand{\@authorOne}{{#1}}}
\newcommand{\@studentnumberOne}{\color{red}\textbf{fill in student number}}
\newcommand{\studentnumberOne}[1]{\renewcommand{\@studentnumberOne}{(#1)}}
\newcommand{\@authorTwo}{}
\newcommand{\authorTwo}[1]{\renewcommand{\@authorTwo}{{#1}}}
\newcommand{\@studentnumberTwo}{\color{red}\textbf{fill in student number}}
\newcommand{\studentnumberTwo}[1]{\renewcommand{\@studentnumberTwo}{(#1)}}


\def\@maketitle{\bgroup\setlength{\parindent}{0pt}
	\begin{flushleft}
		{\large \textsc{Distributed Systems (H0N08A)} \par}
		\vspace{1.5em}
		{\Huge \textit{Report: }{\@assignment} \par}
		\vspace{1.5em}
		{\large {\@authorOne}~\textit{\@studentnumberOne} \par}

		% https://tex.stackexchange.com/a/53091
		\setbox0=\hbox{\@authorTwo\unskip}\ifdim\wd0=0pt
			{\large \textit{no team partner} \par}
		\else {\large {\@authorTwo}~\textit{\@studentnumberTwo} \par}
		\fi\par
				\vspace{0.5em}
		\@date \par
		\vspace{2.5em}
	\end{flushleft}\egroup
}