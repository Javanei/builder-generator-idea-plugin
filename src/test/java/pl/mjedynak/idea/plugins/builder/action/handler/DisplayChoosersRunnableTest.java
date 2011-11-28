package pl.mjedynak.idea.plugins.builder.action.handler;

import com.intellij.codeInsight.generation.PsiElementClassMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import pl.mjedynak.idea.plugins.builder.factory.CreateBuilderDialogFactory;
import pl.mjedynak.idea.plugins.builder.factory.MemberChooserDialogFactory;
import pl.mjedynak.idea.plugins.builder.factory.PsiManagerFactory;
import pl.mjedynak.idea.plugins.builder.factory.ReferenceEditorComboWithBrowseButtonFactory;
import pl.mjedynak.idea.plugins.builder.gui.CreateBuilderDialog;
import pl.mjedynak.idea.plugins.builder.gui.helper.GuiHelper;
import pl.mjedynak.idea.plugins.builder.psi.PsiFieldSelector;
import pl.mjedynak.idea.plugins.builder.psi.PsiHelper;
import pl.mjedynak.idea.plugins.builder.writer.BuilderWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(MockitoJUnitRunner.class)
public class DisplayChoosersRunnableTest {

    @InjectMocks
    private DisplayChoosersRunnable displayChoosersRunnable;
    @Mock
    private PsiClass psiClassFromEditor;
    @Mock
    private Project project;
    @Mock
    private Editor editor;
    @Mock
    private PsiHelper psiHelper;
    @Mock
    private PsiManagerFactory psiManagerFactory;
    @Mock
    private CreateBuilderDialogFactory createBuilderDialogFactory;
    @Mock
    private GuiHelper guiHelper;
    @Mock
    private ReferenceEditorComboWithBrowseButtonFactory referenceEditorComboWithBrowseButtonFactory;
    @Mock
    private PsiFieldSelector psiFieldSelector;
    @Mock
    private MemberChooserDialogFactory memberChooserDialogFactory;
    @Mock
    private BuilderWriter builderWriter;
    @Mock
    private Module module;
    @Mock
    private PsiFile psiFile;
    @Mock
    private PsiDirectory psiDirectory;
    @Mock
    private PsiPackage psiPackage;
    @Mock
    private PsiManager psiManager;
    @Mock
    private CreateBuilderDialog createBuilderDialog;
    @Mock
    private MemberChooser memberChooserDialog;

    private String className = "className";

    private PsiField[] allFields = {};

    private List<PsiElementClassMember> selectedFields = new ArrayList<PsiElementClassMember>();

    @Before
    public void setUp() {
        given(psiHelper.findModuleForPsiElement(psiClassFromEditor)).willReturn(module);
        given(psiHelper.getPsiFileFromEditor(editor, project)).willReturn(psiFile);
        given(psiFile.getContainingDirectory()).willReturn(psiDirectory);
        given(psiHelper.getPackage(psiDirectory)).willReturn(psiPackage);
        given(psiManagerFactory.getPsiManager(project)).willReturn(psiManager);
        given(psiClassFromEditor.getName()).willReturn(className);
        given(createBuilderDialogFactory.createBuilderDialog(className + DisplayChoosersRunnable.BUILDER_SUFFIX, project,
                psiPackage, module, psiHelper, psiManager, referenceEditorComboWithBrowseButtonFactory, guiHelper)).willReturn(createBuilderDialog);
    }

    @Test
    public void shouldDisplayCreateBuilderDialogAndDoNothingWhenOKNotSelected() {
        // given
        given(createBuilderDialog.isOK()).willReturn(false);
        // when
        displayChoosersRunnable.run();
        // then
        verify(createBuilderDialog).show();
        verifyZeroInteractions(psiFieldSelector, memberChooserDialogFactory, builderWriter);
    }


    @Test
    public void shouldNotWriteBuilderWhenOkNotSelectedFromMemberChooserDialog() {
        // given
        given(createBuilderDialog.isOK()).willReturn(true);
        given(memberChooserDialog.isOK()).willReturn(false);
        given(createBuilderDialog.getTargetDirectory()).willReturn(psiDirectory);
        given(createBuilderDialog.getClassName()).willReturn(className);
        given(psiClassFromEditor.getAllFields()).willReturn(allFields);
        given(memberChooserDialogFactory.getMemberChooserDialog(selectedFields, project)).willReturn(memberChooserDialog);

        // when
        displayChoosersRunnable.run();

        // then
        verify(createBuilderDialog).isOK();
        verify(memberChooserDialog).isOK();
        verify(createBuilderDialog).show();
        verifyZeroInteractions(builderWriter);
    }

    @Test
    public void shouldDisplayCreateBuilderAndMemberChooserDialogAndWriteBuilderWhenOKSelectedFromBothWindows() {
        // given
        given(createBuilderDialog.isOK()).willReturn(true);
        given(memberChooserDialog.isOK()).willReturn(true);
        given(createBuilderDialog.getTargetDirectory()).willReturn(psiDirectory);
        given(createBuilderDialog.getClassName()).willReturn(className);
        given(psiClassFromEditor.getAllFields()).willReturn(allFields);
        given(memberChooserDialogFactory.getMemberChooserDialog(selectedFields, project)).willReturn(memberChooserDialog);
        given(psiFieldSelector.selectFieldsToIncludeInBuilder(Arrays.asList(allFields))).willReturn(selectedFields);
        given(memberChooserDialog.getSelectedElements()).willReturn(selectedFields);

        // when
        displayChoosersRunnable.run();

        // then
        verify(createBuilderDialog).isOK();
        verify(memberChooserDialog).isOK();
        verify(createBuilderDialog).show();
        verify(memberChooserDialog).show();
        verify(builderWriter).writeBuilder(project, selectedFields, psiDirectory, className, psiClassFromEditor);
    }
}